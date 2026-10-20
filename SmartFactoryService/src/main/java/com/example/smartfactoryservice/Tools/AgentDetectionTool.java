package com.example.smartfactoryservice.Tools;

import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorConfig;
import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorRecord;
import com.example.smartfactoryservice.Service.AgentModule.AIAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 智能检测核心调用工具。
 * <p>
 * 具备智能路由能力，根据业务场景自动识别并调用适配的业务服务函数。
 * 支持入参校验、异常捕获、服务降级等可靠性机制。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentDetectionTool {

    private final AIAgentService aiAgentService;

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 系统提示词 — 统一角色定义 */
    private static final String SYSTEM_PROMPT_TEMPLATE =
            "你是一位工业传感器数据分析专家。请根据提供的传感器配置（含阈值）和时序数据，"
                    + "一次性完成以下两项分析任务：\n"
                    + "1. **预测性维护 (Predictive Maintenance)**：分析数值变化趋势，预测未来是否可能跨越危险阈值，给出风险等级和预估剩余安全时间。\n"
                    + "2. **智能异常检测 (Anomaly Detection)**：分析传感器数值的波动模式，检测\"数值在阈值内但模式异常\"的场景（如频率突变、缓慢漂移、周期性紊乱等）。\n\n"
                    + "注意：采集的传感器数据时间不连续是正常现象，可能由于传感器开关切换、调试等原因导致数据点之间存在时间间隔，请勿将其视为异常。\n\n"
                    + "报告输出要求：\n"
                    + "- 两者均正常 → 报告以\"【正常】\"为标题，简要覆盖两方面结论。\n"
                    + "- 仅一方异常 → 异常方以\"【异常】\"标题重点详述（问题描述、原因分析、处理建议），正常方以\"【正常】\"简要带过。\n"
                    + "- 两者均异常 → 两部分均以\"【异常】\"标题重点详述。\n"
                    + "- 如有异常，请在结论部分给出具体的风险等级（低/中/高/紧急）和可操作建议。\n"
                    + "输出语言：中文。";

    /** 仅预测性维护的系统提示词 */
    private static final String SYSTEM_PROMPT_PREDICTIVE_ONLY =
            "你是一位工业传感器数据分析专家。请仅针对**预测性维护**进行分析：\n"
                    + "根据传感器配置（含阈值）和时序数据，分析数值变化趋势，预测未来是否可能跨越危险阈值，"
                    + "给出风险等级和预估剩余安全时间。\n"
                    + "注意：采集的传感器数据时间不连续是正常现象，可能由于传感器开关切换、调试等原因导致数据点之间存在时间间隔，请勿将其视为异常。\n"
                    + "输出要求：以\"【预测性维护】\"为分析标题，若正常以\"【正常】\"标记，若异常以\"【异常】\"标记并详述。\n"
                    + "输出语言：中文。";

    /** 仅异常检测的系统提示词 */
    private static final String SYSTEM_PROMPT_ANOMALY_ONLY =
            "你是一位工业传感器数据分析专家。请仅针对**智能异常检测**进行分析：\n"
                    + "分析传感器数值的波动模式，检测\"数值在阈值内但模式异常\"的场景（如频率突变、缓慢漂移、周期性紊乱等）。\n"
                    + "注意：采集的传感器数据时间不连续是正常现象，可能由于传感器开关切换、调试等原因导致数据点之间存在时间间隔，请勿将其视为异常。\n"
                    + "输出要求：以\"【异常检测】\"为分析标题，若正常以\"【正常】\"标记，若异常以\"【异常】\"标记并详述。\n"
                    + "输出语言：中文。";

    /** LLM 调用最大重试次数 */
    private static final int MAX_RETRY = 2;

    /**
     * 业务检测场景枚举 — 用于智能路由
     */
    public enum DetectionScenario {
        /** 全量分析：预测性维护 + 异常检测 */
        FULL,
        /** 仅预测性维护 */
        PREDICTIVE_ONLY,
        /** 仅异常检测 */
        ANOMALY_ONLY
    }

    // ==================== 公开核心方法 ====================

    /**
     * 执行 Agent 智能检测分析（自动识别场景）。
     * <p>
     * 根据传入的场景标识，智能路由到对应分析策略，组装 Prompt 并调用 LLM，返回分析报告。
     * </p>
     *
     * @param scenario      检测场景（FULL / PREDICTIVE_ONLY / ANOMALY_ONLY）
     * @param sensorConfigs 全部传感器配置列表（含阈值）
     * @param sensorRecords 时间窗口内的全部传感器记录
     * @return LLM 分析报告文本；失败时返回 null
     */
    public String analyze(DetectionScenario scenario,
                          List<SensorConfig> sensorConfigs,
                          List<SensorRecord> sensorRecords) {
        // --- 1. 入参校验 ---
        if (!validateInputs(sensorConfigs, sensorRecords)) {
            log.warn("[AgentDetectionTool] 入参校验失败，执行服务降级");
            return buildDegradationReport(sensorConfigs, sensorRecords);
        }

        // --- 2. 智能路由：根据场景选择 System Prompt ---
        String systemPrompt = routeSystemPrompt(scenario);

        // --- 3. 组装 User Prompt ---
        String userPrompt = buildUserPrompt(sensorConfigs, sensorRecords);
        log.info("[AgentDetectionTool] 场景={}, 传感器数={}, 记录数={}",
                scenario, sensorConfigs.size(), sensorRecords.size());

        // --- 4. 调用 LLM（含重试机制） ---
        return callLLMWithRetry(systemPrompt, userPrompt);
    }

    /**
     * 简化调用：默认全量分析场景。
     */
    public String analyzeComprehensive(List<SensorConfig> sensorConfigs,
                                       List<SensorRecord> sensorRecords) {
        return analyze(DetectionScenario.FULL, sensorConfigs, sensorRecords);
    }

    // ==================== 私有：智能路由 ====================

    /**
     * 根据业务场景路由到对应的 System Prompt。
     */
    private String routeSystemPrompt(DetectionScenario scenario) {
        if (scenario == null) {
            return SYSTEM_PROMPT_TEMPLATE;
        }
        switch (scenario) {
            case PREDICTIVE_ONLY:
                return SYSTEM_PROMPT_PREDICTIVE_ONLY;
            case ANOMALY_ONLY:
                return SYSTEM_PROMPT_ANOMALY_ONLY;
            case FULL:
            default:
                return SYSTEM_PROMPT_TEMPLATE;
        }
    }

    // ==================== 私有：Prompt 组装 ====================

    /**
     * 组装 User Prompt：传感器配置 + 时序数据。
     */
    private String buildUserPrompt(List<SensorConfig> configs, List<SensorRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 传感器配置信息\n\n");

        // 按 sensorId 分组配置
        Map<Long, SensorConfig> configMap = configs.stream()
                .collect(Collectors.toMap(SensorConfig::getId, c -> c, (a, b) -> a));

        for (SensorConfig cfg : configs) {
            sb.append(formatSensorConfig(cfg));
        }

        sb.append("\n## 传感器时序数据\n\n");
        if (records.isEmpty()) {
            sb.append("（无记录）\n");
        } else {
            // 按传感器分组展示
            Map<Long, List<SensorRecord>> grouped = records.stream()
                    .collect(Collectors.groupingBy(SensorRecord::getSensorId));
            for (Map.Entry<Long, List<SensorRecord>> entry : grouped.entrySet()) {
                Long sensorId = entry.getKey();
                SensorConfig cfg = configMap.get(sensorId);
                String sensorLabel = cfg != null
                        ? String.format("传感器「%s」(ID=%d)", cfg.getSensorName(), sensorId)
                        : String.format("传感器 ID=%d", sensorId);

                sb.append(String.format("### %s\n", sensorLabel));
                sb.append("| 时间 | 数值 | 状态 |\n");
                sb.append("|------|------|------|\n");
                for (SensorRecord r : entry.getValue()) {
                    sb.append(String.format("| %s | %d | %d |\n",
                            r.getDatetime() != null ? r.getDatetime().format(DT_FORMAT) : "-",
                            r.getValue() != null ? r.getValue() : 0,
                            r.getStatus() != null ? r.getStatus() : 0));
                }
                sb.append("\n");
            }
        }

        sb.append("请基于以上配置和数据进行预测性维护与异常检测分析。");
        return sb.toString();
    }

    private String formatSensorConfig(SensorConfig cfg) {
        return String.format(
                "- 传感器「%s」: 编号=%d, 场景=%d, 量程=[%d, %d], 阈值上限=%d, 阈值中位=%d, 阈值下限=%d\n",
                cfg.getSensorName() != null ? cfg.getSensorName() : "未知",
                cfg.getSensor() != null ? cfg.getSensor() : 0,
                cfg.getSceneType() != null ? cfg.getSceneType() : 0,
                cfg.getMin() != null ? cfg.getMin() : 0,
                cfg.getMax() != null ? cfg.getMax() : 0,
                cfg.getMaxTh() != null ? cfg.getMaxTh() : 0,
                cfg.getMidTh() != null ? cfg.getMidTh() : 0,
                cfg.getMinTh() != null ? cfg.getMinTh() : 0);
    }

    // ==================== 私有：可靠性机制 ====================

    /**
     * 入参校验：检查配置和数据是否有效。
     */
    private boolean validateInputs(List<SensorConfig> sensorConfigs, List<SensorRecord> sensorRecords) {
        if (sensorConfigs == null || sensorConfigs.isEmpty()) {
            log.warn("[AgentDetectionTool] 传感器配置列表为空，无法执行分析");
            return false;
        }
        if (sensorRecords == null) {
            log.warn("[AgentDetectionTool] 传感器记录为 null，无法执行分析");
            return false;
        }
        // 记录为空时可以执行（LLM会告知无数据可分析），但这里记录日志
        if (sensorRecords.isEmpty()) {
            log.info("[AgentDetectionTool] 传感器记录为空，分析可能无实际数据");
        }
        return true;
    }

    /**
     * 带重试的 LLM 调用。
     */
    private String callLLMWithRetry(String systemPrompt, String userPrompt) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt <= MAX_RETRY) {
            try {
                attempt++;
                log.info("[AgentDetectionTool] LLM 调用尝试 {}/{}", attempt, MAX_RETRY + 1);
                long startMs = System.currentTimeMillis();
                String response = aiAgentService.chatWithSystem(systemPrompt, userPrompt);
                long elapsedMs = System.currentTimeMillis() - startMs;
                log.info("[AgentDetectionTool] LLM 调用成功, 耗时={}ms, 响应长度={}",
                        elapsedMs, response != null ? response.length() : 0);
                return response;
            } catch (Exception e) {
                lastException = e;
                log.warn("[AgentDetectionTool] LLM 调用失败 (attempt={}/{}): {}",
                        attempt, MAX_RETRY + 1, e.getMessage());
                if (attempt <= MAX_RETRY) {
                    // 指数退避
                    long backoffMs = (long) Math.pow(2, attempt) * 500;
                    log.info("[AgentDetectionTool] 等待 {}ms 后重试", backoffMs);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }

        log.error("[AgentDetectionTool] LLM 调用全部重试失败，触发服务降级", lastException);
        return null;
    }

    /**
     * 服务降级报告：LLM 不可用时生成的兜底报告。
     */
    private String buildDegradationReport(List<SensorConfig> configs, List<SensorRecord> records) {
        return "【服务降级】AI 分析服务暂时不可用，无法生成智能检测报告。"
                + "请检查 DeepSeek API 连接状态。\n"
                + "配置传感器数: " + (configs != null ? configs.size() : 0)
                + ", 采集记录数: " + (records != null ? records.size() : 0);
    }
}
