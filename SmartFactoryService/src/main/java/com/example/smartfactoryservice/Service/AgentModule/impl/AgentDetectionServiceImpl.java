package com.example.smartfactoryservice.Service.AgentModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smartfactoryservice.Domain.Entity.AgentModule.AgentDetectionResult;
import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorConfig;
import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorRecord;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Employee;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Mapper.AgentModule.AgentDetectionResultMapper;
import com.example.smartfactoryservice.Mapper.EduBoxModule.SensorConfigMapper;
import com.example.smartfactoryservice.Mapper.EduBoxModule.SensorRecordMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.EmployeeMapper;
import com.example.smartfactoryservice.Service.AgentModule.AgentDetectionService;
import com.example.smartfactoryservice.Tools.AgentDetectionTool;
import com.example.smartfactoryservice.Tools.AgentDetectionTool.DetectionScenario;
import com.example.smartfactoryservice.Utils.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 智能检测业务服务实现。
 * <p>
 * 对接 {@link AgentDetectionTool} 核心调用函数，实现完整的自动化检测处理链路：
 * 参数映射 → 数据查询 → LLM 分析 → 结果解析 → 状态流转 → 持久化。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentDetectionServiceImpl implements AgentDetectionService {

    private final AgentDetectionTool agentDetectionTool;
    private final SensorConfigMapper sensorConfigMapper;
    private final SensorRecordMapper sensorRecordMapper;
    private final AgentDetectionResultMapper agentDetectionResultMapper;
    private final EmployeeMapper employeeMapper;

    /** 分析窗口大小（分钟），高频调度下 2 分钟窗口即可提供足够的趋势上下文 */
    private static final long ANALYSIS_WINDOW_MINUTES = 2;

    /** 兜底扩展窗口（分钟）：首次查询为空时，扩大到该范围重试 */
    private static final long FALLBACK_WINDOW_MINUTES = 5;

    // ==================== 核心方法 ====================

    @Override
    public AgentDetectionResult executeDetection() {
        LocalDateTime analysisStart = LocalDateTime.now();

        // --- 1. 参数映射：计算数据时间窗口 ---
        LocalDateTime dataEndTime = analysisStart;
        LocalDateTime dataStartTime = dataEndTime.minusMinutes(ANALYSIS_WINDOW_MINUTES);
        log.info("[AgentDetection] 开始执行检测, 数据窗口: {} ~ {}",
                dataStartTime, dataEndTime);

        // --- 2. 查询传感器配置 ---
        List<SensorConfig> sensorConfigs = queryAllSensorConfigs();

        // --- 3. 查询时间窗口内的传感器记录 ---
        List<SensorRecord> sensorRecords = querySensorRecords(dataStartTime, dataEndTime);

        // --- 4. 调用 Agent 核心分析工具 ---
        String llmReport = agentDetectionTool.analyze(
                DetectionScenario.FULL, sensorConfigs, sensorRecords);

        LocalDateTime analysisEnd = LocalDateTime.now();

        // --- 5. 结果解析与状态流转 ---
        AgentDetectionResult result = buildResult(
                llmReport, dataStartTime, dataEndTime, analysisStart, analysisEnd);

        // --- 6. 持久化 ---
        agentDetectionResultMapper.insert(result);
        log.info("[AgentDetection] 检测完成, id={}, status={}, 耗时={}ms",
                result.getId(), result.getStatus(),
                Duration.between(analysisStart, analysisEnd).toMillis());

        // 检测到异常时，通知部门 3 所有员工
        if (result.getIsNotified() != null && result.getIsNotified() == 1) {
            notifyDepartment3Employees(result);
        }

        return result;
    }

    @Override
    public void markNotified(Long resultId) {
        AgentDetectionResult exist = agentDetectionResultMapper.selectById(resultId);
        if (exist != null && exist.getIsNotified() != 1) {
            exist.setIsNotified(1);
            agentDetectionResultMapper.updateById(exist);
            log.info("[AgentDetection] 标记已通知: id={}", resultId);
        }
    }

    @Override
    public PageBean pageList(String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<AgentDetectionResult> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(AgentDetectionResult::getStatus, status.trim());
        }
        wrapper.orderByDesc(AgentDetectionResult::getCreateTime);

        Page<AgentDetectionResult> page = agentDetectionResultMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        return new PageBean(page.getTotal(), page.getRecords());
    }

    // ==================== 私有：数据查询 ====================

    /**
     * 查询所有未删除的传感器配置。
     */
    private List<SensorConfig> queryAllSensorConfigs() {
        LambdaQueryWrapper<SensorConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensorConfig::getDeleteFlag, false)
                .eq(SensorConfig::getValueType, false); // 仅取数值型传感器，开关量无分析意义
        List<SensorConfig> configs = sensorConfigMapper.selectList(wrapper);
        log.info("[AgentDetection] 查询到 {} 个数值型传感器配置", configs.size());
        return configs;
    }

    /**
     * 查询指定时间窗口内的传感器记录，空结果时自动扩展窗口兜底重试。
     */
    private List<SensorRecord> querySensorRecords(LocalDateTime start, LocalDateTime end) {
        List<SensorRecord> records = doQueryRecords(start, end);

        // 首次查询为空时，扩大窗口兜底重试
        if (records.isEmpty()) {
            log.warn("[AgentDetection] 窗口 ({} ~ {}) 内无记录，扩大窗口至 {} 分钟兜底重试",
                    start, end, FALLBACK_WINDOW_MINUTES);
            LocalDateTime fallbackStart = end.minusMinutes(FALLBACK_WINDOW_MINUTES);
            records = doQueryRecords(fallbackStart, end);
        }
        return records;
    }

    private List<SensorRecord> doQueryRecords(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<SensorRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SensorRecord::getDatetime, start)
                .le(SensorRecord::getDatetime, end)
                .orderByAsc(SensorRecord::getDatetime);
        List<SensorRecord> records = sensorRecordMapper.selectList(wrapper);
        log.info("[AgentDetection] 查询到 {} 条传感器记录 ({} ~ {})",
                records.size(), start, end);
        return records;
    }

    // ==================== 私有：结果解析与状态流转 ====================

    /**
     * 根据 LLM 返回报告构建检测结果实体。
     * <p>
     * 状态流转逻辑：<br>
     * - LLM 返回 null → status=FAILED，报告记录失败原因<br>
     * - LLM 返回正常 → status=SUCCESS，报告标记为正常<br>
     * - LLM 返回含异常 → status=SUCCESS，isNotified 可根据业务需求置为 1
     * </p>
     */
    private AgentDetectionResult buildResult(String llmReport,
                                              LocalDateTime dataStart, LocalDateTime dataEnd,
                                              LocalDateTime analysisStart, LocalDateTime analysisEnd) {
        AgentDetectionResult result = new AgentDetectionResult();
        result.setDataStartTime(dataStart);
        result.setDataEndTime(dataEnd);
        result.setAnalysisStartTime(analysisStart);
        result.setAnalysisEndTime(analysisEnd);
        result.setCreateTime(LocalDateTime.now());

        if (llmReport == null) {
            // LLM 调用失败
            result.setStatus("FAILED");
            result.setResultReport("AI 分析服务调用失败，请检查 DeepSeek API 连接状态。");
            result.setIsNotified(0);
        } else {
            // LLM 调用成功
            result.setStatus("SUCCESS");
            result.setResultReport(llmReport);

            // 根据报告内容判断是否存在异常，决定是否触发通知
            boolean hasAnomaly = detectAnomalyFromReport(llmReport);
            result.setIsNotified(hasAnomaly ? 1 : 0);
            log.info("[AgentDetection] 异常检测结果: hasAnomaly={}", hasAnomaly);
        }
        return result;
    }

    /**
     * 从 LLM 报告中检测是否存在异常。
     * <p>
     * 解析策略：统计报告中【异常】标记出现的次数。
     * 若报告中不存在【异常】标记，则判定为正常。
     * 同时检查降级报告和服务降级关键词。
     * </p>
     */
    private boolean detectAnomalyFromReport(String report) {
        if (report == null || report.isEmpty()) {
            return false;
        }
        // 服务降级报告不视为异常（已是 FAILED 状态）
        if (report.contains("服务降级")) {
            return false;
        }
        // 统计【异常】标记出现次数（排除【正常】段落内的）
        // 简化策略：只要出现至少一次【异常】标记即为存在异常
        int anomalyCount = countOccurrences(report, "【异常】");
        return anomalyCount > 0;
    }

    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(keyword, idx)) != -1) {
            count++;
            idx += keyword.length();
        }
        return count;
    }

    // ==================== 私有：邮件通知 ====================

    /**
     * 查询部门 ID 为 3 的所有员工，发送异常检测报告邮件。
     */
    private void notifyDepartment3Employees(AgentDetectionResult result) {
        List<Employee> employees = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>()
                        .eq(Employee::getDepartmentId, 3)
                        .eq(Employee::isDeleteFlag, false));

        if (employees.isEmpty()) {
            log.info("[AgentDetection] 部门 3 无员工，跳过邮件通知");
            return;
        }

        String subject = "【智能工厂】Agent 异常检测报告";
        String html = buildNotificationHtml(result);

        for (Employee emp : employees) {
            if (emp.getEmail() == null || emp.getEmail().isBlank()) {
                log.warn("[AgentDetection] 员工 {} 未设置邮箱，跳过", emp.getName());
                continue;
            }
            EmailUtil.sendHtmlMail(emp.getEmail(), subject, html);
        }
        log.info("[AgentDetection] 已向部门 3 的 {} 名员工发送异常通知邮件", employees.size());
    }

    /**
     * 构建通知邮件 HTML 内容。
     */
    private String buildNotificationHtml(AgentDetectionResult result) {
        return "<h3>Agent 智能检测异常报告</h3>"
                + "<p><b>检测时间：</b>" + result.getCreateTime() + "</p>"
                + "<p><b>数据窗口：</b>" + result.getDataStartTime() + " ~ " + result.getDataEndTime() + "</p>"
                + "<p><b>状态：</b>" + result.getStatus() + "</p>"
                + "<hr/>"
                + "<pre style='white-space:pre-wrap;font-family:inherit;'>"
                + (result.getResultReport() != null ? result.getResultReport() : "无")
                + "</pre>";
    }
}
