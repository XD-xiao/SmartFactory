package com.example.smartfactoryservice.Controller.AgentModule;

import com.example.smartfactoryservice.Domain.Entity.AgentModule.AgentDetectionResult;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.AgentModule.AgentDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 智能检测结果控制层。
 * 提供检测报告的分页查询、详情查看接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agent")
public class AgentDetectionController {

    private final AgentDetectionService agentDetectionService;

    /**
     * 分页查询 Agent 检测结果列表。
     *
     * @param status   检测状态筛选（SUCCESS/FAILED/UNREAD/READ），可选
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 10
     */
    @GetMapping("/getDetectionList")
    public Result<PageBean> getDetectionList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        return Result.success("获取检测记录列表成功",
                agentDetectionService.pageList(status, pageNum, pageSize));
    }
}
