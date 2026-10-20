package com.example.smartfactoryservice.Service.AgentModule;

import com.example.smartfactoryservice.Domain.Entity.AgentModule.AgentDetectionResult;
import com.example.smartfactoryservice.Domain.Vo.PageBean;

/**
 * Agent 智能检测业务服务。
 * <p>
 * 对接 AgentDetectionTool 核心调用函数，实现自动化检测处理链路，
 * 包含参数映射、结果解析、状态流转等核心能力。
 * </p>
 */
public interface AgentDetectionService {

    /**
     * 执行一次完整的 Agent 智能检测分析。
     * <p>
     * 流程：查询配置 → 查询记录 → 调用 LLM → 解析结果 → 持久化。
     * </p>
     *
     * @return 本次检测结果实体；调用失败时 status=FAILED
     */
    AgentDetectionResult executeDetection();

    /**
     * 将某条检测结果标记为已通知。
     *
     * @param resultId 检测结果 ID
     */
    void markNotified(Long resultId);

    /**
     * 分页查询 Agent 检测结果列表，支持按状态筛选。
     *
     * @param status   检测状态（SUCCESS/FAILED），传 null 表示不过滤
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageBean pageList(String status, int pageNum, int pageSize);
}
