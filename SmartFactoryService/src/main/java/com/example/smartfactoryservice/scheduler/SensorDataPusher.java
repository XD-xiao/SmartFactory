package com.example.smartfactoryservice.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorConfig;
import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorRecord;
import com.example.smartfactoryservice.Domain.Vo.Sensor.SensorVO;
import com.example.smartfactoryservice.Mapper.EduBoxModule.SensorConfigMapper;
import com.example.smartfactoryservice.Mapper.EduBoxModule.SensorRecordMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * 大屏传感器数据推送任务。
 * 仅在有客户端订阅 /topic/sensor 时才执行查询和推送，
 * 最后一个订阅者离开后自动停止。
 * 仅推送 valueType=0（开关量）的传感器数据。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataPusher {

    private static final String TOPIC = "/topic/sensor";
    private static final long INTERVAL_MS = 3000;

    /** 每次推送每个传感器保留的最新记录数 */
    private static final int MAX_RECORDS_PER_SENSOR = 30;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final SimpMessagingTemplate messagingTemplate;
    private final SensorConfigMapper sensorConfigMapper;
    private final SensorRecordMapper sensorRecordMapper;
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
                pushSensorData();
                scheduledFuture = taskScheduler.scheduleAtFixedRate(
                        this::pushSensorData,
                        new Date(System.currentTimeMillis() + INTERVAL_MS),
                        INTERVAL_MS);
                log.info("传感器数据推送已启动: topic={}, interval={}ms", TOPIC, INTERVAL_MS);
            }
        }
    }

    private void stopPushing() {
        synchronized (lock) {
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
                scheduledFuture = null;
                log.info("传感器数据推送已停止: topic={}", TOPIC);
            }
        }
    }

    private void pushSensorData() {
        try {
            SensorVO data = buildSensorData();
            messagingTemplate.convertAndSend(TOPIC, data);
            log.debug("传感器数据推送成功: sensors={}",
                    data.getSensors() != null ? data.getSensors().size() : 0);
        } catch (Exception e) {
            log.error("推送传感器数据失败", e);
        }
    }

    private SensorVO buildSensorData() {
        // 查询所有未删除且 valueType=0（开关量）的传感器配置
        LambdaQueryWrapper<SensorConfig> configWrapper = new LambdaQueryWrapper<>();
        configWrapper.eq(SensorConfig::getDeleteFlag, false)
                .eq(SensorConfig::getValueType, false);
        List<SensorConfig> configs = sensorConfigMapper.selectList(configWrapper);

        if (configs.isEmpty()) {
            return new SensorVO(new ArrayList<>());
        }

        List<SensorVO.SensorItem> sensors = new ArrayList<>();

        for (SensorConfig config : configs) {
            // 查询该传感器的最新记录（按时间降序取前N条，再升序排列）
            LambdaQueryWrapper<SensorRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(SensorRecord::getSensorId, config.getId())
                    .orderByDesc(SensorRecord::getDatetime)
                    .last("LIMIT " + MAX_RECORDS_PER_SENSOR);
            List<SensorRecord> records = sensorRecordMapper.selectList(recordWrapper);

            // 转为升序（前端折线图从左到右展示最新）
            records.sort(Comparator.comparing(SensorRecord::getDatetime));

            List<SensorVO.RecordPoint> recordPoints = records.stream()
                    .map(r -> new SensorVO.RecordPoint(
                            r.getValue(),
                            r.getStatus() != null ? r.getStatus() : 0,
                            r.getDatetime() != null
                                    ? r.getDatetime().format(TIME_FORMATTER)
                                    : ""))
                    .collect(Collectors.toList());

            Integer sceneType = config.getSceneType() != null
                    ? config.getSceneType().intValue() : null;

            sensors.add(new SensorVO.SensorItem(
                    config.getId(),
                    config.getSensorName() != null ? config.getSensorName() : "传感器",
                    sceneType != null ? sceneType.longValue() : 0L,
                    recordPoints));
        }

        return new SensorVO(sensors);
    }
}
