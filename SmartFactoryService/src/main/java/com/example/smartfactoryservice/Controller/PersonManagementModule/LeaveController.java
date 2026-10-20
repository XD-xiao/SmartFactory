package com.example.smartfactoryservice.Controller.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.PersonManagementModule.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pm")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/addLeaveRecord")
    public Result<Void> addLeaveRecord(@RequestBody(required = false) Map<String, Object> leaveRecord) {
        leaveService.addLeaveRecord(leaveRecord);
        return Result.success("添加请假记录成功", null);
    }

    @PutMapping("/updateLeaveRecord")
    public Result<Void> updateLeaveRecord(@RequestBody(required = false) Map<String, Object> leaveRecord) {
        leaveService.updateLeaveRecord(leaveRecord);
        return Result.success("修改请假记录成功", null);
    }

    @GetMapping("/getLeaveList")
    public Result<PageBean> getLeaveList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(name = "employeeNumber", required = false) String employeeNumber,
            @RequestParam(required = false) Integer leaveType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return Result.success("获取请假记录列表成功",
                leaveService.getLeaveList(pageNum, pageSize, employeeNumber, leaveType, status, startDate, endDate));
    }

    @DeleteMapping("/deleteLeave")
    public Result<Void> deleteLeave(@RequestParam(name = "leaveID", required = false) Integer leaveId) {
        leaveService.deleteLeave(leaveId);
        return Result.success("删除请假记录成功", null);
    }
}
