package com.example.smartfactoryservice.Service.DeviceModule.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smartfactoryservice.Controller.NotificationController;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceEquipmentRunTime;
import com.example.smartfactoryservice.Domain.Mqtt.PlcSwitchMqttPayload;
import com.example.smartfactoryservice.Domain.Mqtt.PlcSyncMqttPayload;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceEquipmentRunTimeMapper;
import com.example.smartfactoryservice.Service.DeviceModule.PlcService;
import com.example.smartfactoryservice.Service.MqttMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlcServiceImpl implements PlcService {

    private final DeviceEquipmentRunTimeMapper deviceEquipmentRunTimeMapper;

    private final NotificationController notificationController;

    private final MqttMessageSender mqttMessageSender;

    @Override
    public PageBean getPlcList(String sn, Integer seqNo, int pageNum, int pageSize) {
        QueryWrapper<DeviceEquipmentRunTime> wrapper = new QueryWrapper<>();
        if (sn != null && !sn.isEmpty()) {
            wrapper.eq("sn", sn);
        }
        if (seqNo != null) {
            wrapper.eq("seq_no", seqNo);
        }
        wrapper.orderByDesc("id");

        Page<DeviceEquipmentRunTime> page = new Page<>(pageNum, pageSize);
        Page<DeviceEquipmentRunTime> result = deviceEquipmentRunTimeMapper.selectPage(page, wrapper);

        Long total = deviceEquipmentRunTimeMapper.selectCount(wrapper);

        return new PageBean(total, result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRunTime(PlcSwitchMqttPayload payload) {
        DeviceEquipmentRunTime latest = findLatest(payload.getSn(), payload.getIndex());

        if (payload.getAction() == 1) {
            handleActionStart(payload, latest);
        } else if (payload.getAction() == 0) {
            handleActionStop(payload, latest);
        }

        // 推送当前所有产线状态到前端
        int[] currentStates = getCurrentDeviceStates(payload.getSn());
        notificationController.pushPlcDeviceStatus(payload.getSn(), currentStates);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSyncRunTimes(PlcSyncMqttPayload payload) {
        int[] device = payload.getDevice();
        if (device == null || device.length == 0) {
            return;
        }

        String sn = payload.getSn();
        for (int i = 0; i < device.length; i++) {
            DeviceEquipmentRunTime latest = findLatest(sn, i);

            if (device[i] == 1) {
                handleActionStart(sn, i, latest);
            } else if (device[i] == 0) {
                handleActionStop(sn, i, latest);
            }
        }

        // 处理完成后推送各产线状态到前端
        notificationController.pushPlcDeviceStatus(sn, device);
    }

    @Override
    public void controlPlc(String sn, Integer index, Integer action) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sn", sn);
        payload.put("index", index);
        payload.put("action", action);
        mqttMessageSender.sendMsg("order/adapter/action", JSON.toJSONString(payload));
    }

    /**
     * 查找该设备该序号的最新一条记录
     */
    private DeviceEquipmentRunTime findLatest(String sn, Integer index) {
        QueryWrapper<DeviceEquipmentRunTime> wrapper = new QueryWrapper<>();
        wrapper.eq("sn", sn)
                .eq("seq_no", index)
                .orderByDesc("id")
                .last("LIMIT 1");
        return deviceEquipmentRunTimeMapper.selectOne(wrapper);
    }

    /**
     * 获取指定 SN 下所有产线的当前开关状态
     * @return int[] 下标为 seqNo，值为 action（0-关, 1-开）
     */
    private int[] getCurrentDeviceStates(String sn) {
        QueryWrapper<DeviceEquipmentRunTime> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT seq_no").eq("sn", sn);
        List<Object> seqNos = deviceEquipmentRunTimeMapper.selectObjs(wrapper);

        if (seqNos == null || seqNos.isEmpty()) {
            return new int[0];
        }

        int maxSeqNo = seqNos.stream().mapToInt(o -> (Integer) o).max().orElse(0);
        int[] devices = new int[maxSeqNo + 1];

        for (Object obj : seqNos) {
            Integer seqNo = (Integer) obj;
            DeviceEquipmentRunTime latest = findLatest(sn, seqNo);
            if (latest != null) {
                devices[seqNo] = latest.getAction();
            }
        }

        return devices;
    }

    /**
     * 处理开机：无记录或已完成则新增，进行中则忽略
     */
    private void handleActionStart(PlcSwitchMqttPayload payload, DeviceEquipmentRunTime latest) {
        handleActionStart(payload.getSn(), payload.getIndex(), latest);
    }

    private void handleActionStart(String sn, Integer index, DeviceEquipmentRunTime latest) {
        // 如果存在进行中的记录（未结束），先将其关闭
        if (latest != null && latest.getWorkEndTime() == null) {
            LocalDateTime now = LocalDateTime.now();
            float workTime = (float) Duration.between(latest.getWorkBeginTime(), now).getSeconds() / 3600;
            latest.setAction(0);
            latest.setWorkEndTime(now);
            latest.setWorkTime(workTime);
            deviceEquipmentRunTimeMapper.updateById(latest);
        }
        // 新增一条开机记录
        DeviceEquipmentRunTime entity = new DeviceEquipmentRunTime();
        entity.setSn(sn);
        entity.setSeqNo(index);
        entity.setAction(1);
        entity.setWorkBeginTime(LocalDateTime.now());
        deviceEquipmentRunTimeMapper.insert(entity);
    }

    /**
     * 处理关机：进行中则更新结束时间和时长，否则忽略
     */
    private void handleActionStop(PlcSwitchMqttPayload payload, DeviceEquipmentRunTime latest) {
        handleActionStop(payload.getSn(), payload.getIndex(), latest);
    }

    private void handleActionStop(String sn, Integer index, DeviceEquipmentRunTime latest) {
        if (latest != null && latest.getWorkEndTime() == null) {
            LocalDateTime now = LocalDateTime.now();
            float workTime = (float) Duration.between(latest.getWorkBeginTime(), now).getSeconds() / 3600;

            latest.setAction(0);
            latest.setWorkEndTime(now);
            latest.setWorkTime(workTime);
            deviceEquipmentRunTimeMapper.updateById(latest);
        }
    }
}
