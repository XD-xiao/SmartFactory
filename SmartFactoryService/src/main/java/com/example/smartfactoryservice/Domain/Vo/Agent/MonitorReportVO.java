package com.example.smartfactoryservice.Domain.Vo.Agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 监测报告 WebSocket 推送 VO。
 * <p>
 * 用于将 Agent 智能检测结果通过 {@code /topic/monitor-report} 推送到大屏前端组件。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorReportVO {

    /** 告警总数 */
    private int totalAlerts;

    /** 预警数（SUCCESS + isNotified=1） */
    private int warnCount;

    /** 异常数（FAILED） */
    private int abnormalCount;

    /** 报告记录列表 */
    private List<ReportRecord> records;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportRecord {
        /** 数据库记录 ID */
        private Long id;

        /**
         * 状态等级：1 = 预警，0 = 正常。
         * 前端 LEVEL_MAP: 1→预警, 其他→正常。
         */
        private int level;

        /** 报告标题 */
        private String title;

        /** 报告正文（AI 分析结果全文） */
        private String reportContent;

        /** 报告来源 */
        private String source;

        /** 报告时间（格式化字符串） */
        private String time;
    }
}
