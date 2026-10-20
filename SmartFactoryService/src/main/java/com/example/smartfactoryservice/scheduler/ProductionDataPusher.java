package com.example.smartfactoryservice.scheduler;

import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceEquipmentRunTime;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroupDevice;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroup;
import com.example.smartfactoryservice.Domain.Vo.Production.ProductionVO;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceEquipmentRunTimeMapper;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupDeviceMapper;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * 大屏产线状态推送任务。
 * 仅在有客户端订阅 /topic/production 时才执行查询和推送，
 * 最后一个订阅者离开后自动停止。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionDataPusher {

    private static final String TOPIC = "/topic/production";
    private static final long INTERVAL_MS = 3000;

    private final SimpMessagingTemplate messagingTemplate;
    private final DeviceEquipmentRunTimeMapper deviceEquipmentRunTimeMapper;
    private final DeviceGroupDeviceMapper deviceGroupDeviceMapper;
    private final DeviceGroupMapper deviceGroupMapper;
    private final SubscriptionTracker subscriptionTracker;
    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledFuture;
    private final Object lock = new Object();

    @PostConstruct
    public void init() {
        subscriptionTracker.register(TOPIC, this::startPushing, this::stopPushing);
    }

    private void startPushing() {
        synchronized (lock) {
            if (scheduledFuture == null || scheduledFuture.isCancelled()) {
                // 立即推送首帧数据，消除首次 3s 延迟
                pushProductionData();
                scheduledFuture = taskScheduler.scheduleAtFixedRate(
                        this::pushProductionData,
                        new Date(System.currentTimeMillis() + INTERVAL_MS),
                        INTERVAL_MS);
                log.info("产线数据推送已启动: topic={}, interval={}ms", TOPIC, INTERVAL_MS);
            }
        }
    }

    private void stopPushing() {
        synchronized (lock) {
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
                scheduledFuture = null;
                log.info("产线数据推送已停止: topic={}", TOPIC);
            }
        }
    }

    private void pushProductionData() {
        try {
            ProductionVO data = buildProductionData();
            messagingTemplate.convertAndSend(TOPIC, data);
            log.debug("产线数据推送成功: running={}, stopped={}, lines={}",
                    data.getRunningCount(), data.getStoppedCount(),
                    data.getLines() != null ? data.getLines().size() : 0);
        } catch (Exception e) {
            log.error("推送产线数据失败", e);
        }
    }

    private ProductionVO buildProductionData() {
        // 查询设备组与设备的关联关系，用于获取产线名称
        List<DeviceGroupDevice> groupDevices = deviceGroupDeviceMapper.selectList(null);
        Map<String, String> snToGroupName = new LinkedHashMap<>();
        if (!groupDevices.isEmpty()) {
            List<DeviceGroup> groups = deviceGroupMapper.selectList(null);
            Map<Long, String> groupIdToName = groups.stream()
                    .collect(Collectors.toMap(DeviceGroup::getId, DeviceGroup::getName, (a, b) -> a));
            for (DeviceGroupDevice gd : groupDevices) {
                String groupName = groupIdToName.get(gd.getGroupId());
                if (groupName != null) {
                    snToGroupName.put(gd.getDeviceSn(), groupName);
                }
            }
        }

        // 查询所有运行记录，获取每个 (sn, seq_no) 的最新状态
        List<DeviceEquipmentRunTime> allRecords = deviceEquipmentRunTimeMapper.selectList(null);

        // 按 (sn, seq_no) 分组，取每组中 id 最大的记录（最新状态）
        Map<String, DeviceEquipmentRunTime> latestMap = new LinkedHashMap<>();
        for (DeviceEquipmentRunTime r : allRecords) {
            String key = r.getSn() + "_" + r.getSeqNo();
            DeviceEquipmentRunTime existing = latestMap.get(key);
            if (existing == null || r.getId() > existing.getId()) {
                latestMap.put(key, r);
            }
        }

        // 按 sn 排序后构建产线列表
        List<ProductionVO.LineItem> lines = new ArrayList<>();
        for (DeviceEquipmentRunTime latest : latestMap.values()) {
            String sn = latest.getSn();
            Integer seqNo = latest.getSeqNo();
            int status = latest.getAction() != null && latest.getAction() == 1 ? 1 : 0;

            // 构建产线名称
            String name;
            String groupName = snToGroupName.get(sn);
            if (groupName != null && !groupName.isBlank()) {
                name = groupName + "-" + String.format("%02d", seqNo);
            } else {
                name = "产线 " + sn + "-" + String.format("%02d", seqNo);
            }

            // 运行开始时间
            String startTime = null;
            if (status == 1 && latest.getWorkBeginTime() != null) {
                startTime = latest.getWorkBeginTime().toString();
            }

            lines.add(new ProductionVO.LineItem(seqNo, name, status, startTime));
        }

        // 按序号排序
        lines.sort(Comparator.comparingInt(ProductionVO.LineItem::getId));

        int runningCount = (int) lines.stream().filter(l -> l.getStatus() == 1).count();
        int stoppedCount = lines.size() - runningCount;

        return new ProductionVO(runningCount, stoppedCount, lines);
    }
}
