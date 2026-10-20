package com.example.smartfactoryservice.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.AgentModule.AgentDetectionResult;
import com.example.smartfactoryservice.Domain.Vo.Agent.MonitorReportVO;
import com.example.smartfactoryservice.Mapper.AgentModule.AgentDetectionResultMapper;
import com.example.smartfactoryservice.Service.AgentModule.AgentDetectionService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * Agent 智能检测定时任务调度器。
 * <p>
 * 按照设计文档指定的触发周期（每 5 分钟），定时调度 Agent 启动业务功能的自动化分析与执行。
 * 支持任务幂等性、执行日志记录、异常告警等运维能力，确保定时任务可监控、可追溯。
 * </p>
 * <p>
 * 监测报告推送仅在有客户端订阅 /topic/monitor-report 时才执行，
 * 最后一个订阅者离开后自动停止。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deepseek.scheduler-enabled", havingValue = "true")
public class AgentDetectionScheduler {

    private static final String REPORT_TOPIC = "/topic/monitor-report";

    private final AgentDetectionService agentDetectionService;
    private final AgentDetectionResultMapper agentDetectionResultMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final SubscriptionTracker subscriptionTracker;
    private final TaskScheduler taskScheduler;

    /** 任务执行间隔（毫秒）= 30 秒，高频轮询确保检测实时性 */
    private static final long INTERVAL_MS = 30_000;

    /** 幂等性保护窗口（毫秒）：防止毫秒级重复触发，远小于调度间隔 */
    private static final long IDEMPOTENT_WINDOW_MS = 10_000;

    /** 报告推送间隔（毫秒）= 5 秒，确保新报告及时推送到大屏 */
    private static final long REPORT_PUSH_INTERVAL_MS = 5_000;

    /** 报告时间格式化器 */
    private static final DateTimeFormatter REPORT_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 报告窗口（分钟）：只推送在此窗口内创建的最新记录 */
    private static final long REPORT_WINDOW_MINUTES = 5;

    /** 上一次执行时间戳，用于幂等性判断 */
    private volatile long lastExecutionTimestamp = 0;

    /** 上一次推送的记录 ID，用于避免同一条记录被反复标记 READ */
    private volatile long lastPushedReportId = 0;

    /** 报告推送的调度句柄 */
    private ScheduledFuture<?> reportFuture;
    private final Object reportLock = new Object();

    @PostConstruct
    public void init() {
        subscriptionTracker.register(REPORT_TOPIC, this::startReportPushing, this::stopReportPushing);
    }

    // ==================== 报告推送生命周期管理 ====================

    private void startReportPushing() {
        synchronized (reportLock) {
            if (reportFuture == null || reportFuture.isCancelled()) {
                // 立即推送首帧数据，消除首次 5s 延迟
                pushMonitoringReport();
                reportFuture = taskScheduler.scheduleAtFixedRate(
                        this::pushMonitoringReport,
                        new Date(System.currentTimeMillis() + REPORT_PUSH_INTERVAL_MS),
                        REPORT_PUSH_INTERVAL_MS);
                log.info("监测报告推送已启动: topic={}, interval={}ms", REPORT_TOPIC, REPORT_PUSH_INTERVAL_MS);
            }
        }
    }

    private void stopReportPushing() {
        synchronized (reportLock) {
            if (reportFuture != null && !reportFuture.isCancelled()) {
                reportFuture.cancel(false);
                reportFuture = null;
                log.info("监测报告推送已停止: topic={}", REPORT_TOPIC);
            }
        }
    }

    // ==================== Agent 检测（始终在后台执行） ====================

    /**
     * 定时触发 Agent 智能检测分析（每 30 秒一次）。
     * <p>
     * 执行流程：<br>
     * 1. 幂等性检查 — 避免短时间重复触发<br>
     * 2. 调用业务服务执行检测<br>
     * 3. 记录执行日志<br>
     * 4. 异常捕获与告警
     * </p>
     */
    @Scheduled(fixedRate = INTERVAL_MS)
    public void executeAgentDetection() {
        // --- 幂等性检查 ---
        long now = System.currentTimeMillis();
        if (now - lastExecutionTimestamp < IDEMPOTENT_WINDOW_MS) {
            log.debug("[AgentDetectionScheduler] 幂等性拦截: 距上次执行仅 {}ms，跳过本次调度",
                    now - lastExecutionTimestamp);
            return;
        }
        lastExecutionTimestamp = now;

        log.info("[AgentDetectionScheduler] ========== Agent 智能检测任务开始 ==========");
        long taskStartMs = System.currentTimeMillis();

        try {
            // --- 执行业务逻辑 ---
            AgentDetectionResult result = agentDetectionService.executeDetection();

            long elapsedMs = System.currentTimeMillis() - taskStartMs;
            log.info("[AgentDetectionScheduler] 任务执行成功, id={}, status={}, 耗时={}ms",
                    result.getId(), result.getStatus(), elapsedMs);

            // --- 异常告警：当存在检测异常时输出告警日志 ---
            if ("SUCCESS".equals(result.getStatus()) && result.getIsNotified() == 1) {
                log.warn("[AgentDetectionScheduler] 检测到异常，已触发通知! id={}, 数据窗口: {} ~ {}",
                        result.getId(), result.getDataStartTime(), result.getDataEndTime());
            } else if ("FAILED".equals(result.getStatus())) {
                log.error("[AgentDetectionScheduler] AI 分析调用失败! id={}, 请检查 DeepSeek API 连接",
                        result.getId());
            }

        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - taskStartMs;
            log.error("[AgentDetectionScheduler] 任务执行异常! 耗时={}ms, error={}",
                    elapsedMs, e.getMessage(), e);
            // 异常不影响下次调度，由 Spring Task 框架兜底
        }

        log.info("[AgentDetectionScheduler] ========== Agent 智能检测任务结束 ==========");
    }

    // ==================== 报告推送 ====================

    /**
     * 推送监测报告到前端大屏。
     * 仅在有客户端订阅 /topic/monitor-report 时被调用。
     * <p>
     * 改进点：<br>
     * 1. 查询最近一条记录（不限定 SUCCESS），确保 MonitoringReport 延迟挂载也能收到数据<br>
     * 2. 仅状态为 SUCCESS 时执行状态流转（→ READ）<br>
     * 3. 携带 AI 分析报告正文（resultReport）<br>
     * </p>
     */
    private void pushMonitoringReport() {
        try {
            // --- 1. 查询统计汇总 ---
            int totalAlerts = countTotalAlerts();
            int warnCount = countWarnAlerts();
            int abnormalCount = countAbnormalAlerts();

            // --- 2. 查询最近一条记录（SUCCESS 或 READ，在窗口期内） ---
            AgentDetectionResult latest = queryLatestInWindow();

            MonitorReportVO vo;
            if (latest != null) {
                MonitorReportVO.ReportRecord record = buildReportRecord(latest);
                vo = new MonitorReportVO(totalAlerts, warnCount, abnormalCount,
                        Collections.singletonList(record));

                messagingTemplate.convertAndSend(REPORT_TOPIC, vo);
                log.info("[AgentDetectionScheduler] 报告推送成功: id={}, level={}, totalAlerts={}",
                        latest.getId(), record.getLevel(), totalAlerts);

                // --- 3. 仅 SUCCESS 执行首次状态流转（避免重复标记） ---
                if ("SUCCESS".equals(latest.getStatus()) && latest.getId() != lastPushedReportId) {
                    markAsRead(latest.getId());
                }
            } else {
                // 无窗口期内记录时推送空报告
                vo = new MonitorReportVO(totalAlerts, warnCount, abnormalCount,
                        Collections.emptyList());
                messagingTemplate.convertAndSend(REPORT_TOPIC, vo);
                log.debug("[AgentDetectionScheduler] 报告推送（空）: totalAlerts={}", totalAlerts);
            }
        } catch (Exception e) {
            log.error("[AgentDetectionScheduler] 报告推送异常: {}", e.getMessage(), e);
        }
    }

    // ==================== 报告推送辅助方法 ====================

    /**
     * 查询最近一条记录（SUCCESS 或 READ，且创建时间在报告窗口内）。
     * 不限定 status=SUCCESS，使延迟挂载的 MonitoringReport 也能收到已推送数据。
     */
    private AgentDetectionResult queryLatestInWindow() {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(REPORT_WINDOW_MINUTES);
        LambdaQueryWrapper<AgentDetectionResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AgentDetectionResult::getStatus, "SUCCESS", "READ")
                .ge(AgentDetectionResult::getCreateTime, windowStart)
                .orderByDesc(AgentDetectionResult::getCreateTime)
                .last("LIMIT 1");
        return agentDetectionResultMapper.selectOne(wrapper);
    }

    /**
     * 将指定记录状态更新为 READ（仅首次标记，避免重复写入）。
     */
    private void markAsRead(Long resultId) {
        AgentDetectionResult record = agentDetectionResultMapper.selectById(resultId);
        if (record != null && "SUCCESS".equals(record.getStatus())) {
            record.setStatus("READ");
            agentDetectionResultMapper.updateById(record);
            lastPushedReportId = resultId;
            log.info("[AgentDetectionScheduler] 记录已标记为 READ: id={}", resultId);
        }
    }

    /**
     * 从检测结果实体构建前端报告记录（含 AI 分析报告正文）。
     */
    private MonitorReportVO.ReportRecord buildReportRecord(AgentDetectionResult result) {
        int level = (result.getIsNotified() != null && result.getIsNotified() == 1) ? 1 : 0;
        String title = (result.getIsNotified() != null && result.getIsNotified() == 1)
                ? "AI 检测异常预警"
                : "AI 检测结果正常";
        String source = "Agent 智能检测";
        String time = result.getCreateTime() != null
                ? result.getCreateTime().format(REPORT_TIME_FMT)
                : "";
        String reportContent = result.getResultReport() != null
                ? result.getResultReport()
                : "";
        return new MonitorReportVO.ReportRecord(result.getId(), level, title,
                reportContent, source, time);
    }

    /**
     * 统计告警总数（所有记录条数）。
     */
    private int countTotalAlerts() {
        LambdaQueryWrapper<AgentDetectionResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(AgentDetectionResult::getId);
        Long count = agentDetectionResultMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 统计预警数（SUCCESS + isNotified=1）。
     */
    private int countWarnAlerts() {
        LambdaQueryWrapper<AgentDetectionResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentDetectionResult::getStatus, "SUCCESS")
                .eq(AgentDetectionResult::getIsNotified, 1);
        Long count = agentDetectionResultMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 统计异常数（FAILED）。
     */
    private int countAbnormalAlerts() {
        LambdaQueryWrapper<AgentDetectionResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentDetectionResult::getStatus, "FAILED");
        Long count = agentDetectionResultMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 手动触发检测（可通过外部接口调用）。
     * <p>
     * 不受幂等性窗口限制，适用于手动调试或补漏场景。
     * </p>
     *
     * @return 检测结果
     */
    public AgentDetectionResult triggerManually() {
        log.info("[AgentDetectionScheduler] 手动触发 Agent 智能检测");
        return agentDetectionService.executeDetection();
    }

    /**
     * 查询最近一次成功执行的检测结果，用于监控面板。
     *
     * @return 最近的检测结果，无记录时返回 null
     */
    public AgentDetectionResult getLatestResult() {
        LambdaQueryWrapper<AgentDetectionResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentDetectionResult::getStatus, "SUCCESS")
                .orderByDesc(AgentDetectionResult::getCreateTime)
                .last("LIMIT 1");
        return agentDetectionResultMapper.selectOne(wrapper);
    }
}
