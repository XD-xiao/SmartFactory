package com.example.smartfactoryservice.Controller.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.PersonManagementModule.AttendanceService;
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
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/addCheckRecord")
    public Result<Void> addCheckRecord(@RequestBody(required = false) Map<String, Object> attendance) {
        attendanceService.addCheckRecord(attendance);
        return Result.success("添加考勤记录成功", null);
    }

    @PutMapping("/updateCheckRecord")
    public Result<Void> updateCheckRecord(@RequestBody(required = false) Map<String, Object> attendance) {
        Integer attendanceID = (Integer) attendance.get("attendanceID");
        attendanceService.updateCheckRecord(attendanceID);
        return Result.success("修改考勤记录成功", null);
    }

    @DeleteMapping("/delCheckRecord")
    public Result<Void> delCheckRecord(@RequestParam(name = "attendanceID", required = false) Integer attendanceId) {
        attendanceService.deleteCheckRecord(attendanceId);
        return Result.success("删除考勤记录成功", null);
    }

    @GetMapping("/getCheckRecordList")
    public Result<PageBean> getCheckRecordList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(name = "employeeNumber", required = false) String employeeNumber,
            @RequestParam(required = false) String date,
            @RequestParam(name = "checkinTime", required = false) String checkinTime,
            @RequestParam(name = "checkoutTime", required = false) String checkoutTime,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer workHours
    ) {
        return Result.success("获取考勤记录列表成功",
                attendanceService.getCheckRecordList(pageNum, pageSize, employeeNumber, date, checkinTime, checkoutTime, status, workHours));
    }
}
