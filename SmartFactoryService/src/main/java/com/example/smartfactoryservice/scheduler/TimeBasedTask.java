package com.example.smartfactoryservice.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.AttendanceRecord;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.AttendanceRecordMapper;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.smartfactoryservice.Service.PersonManagementModule.AttendanceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class TimeBasedTask {

    private final AttendanceService attendanceService;
    private final AttendanceRecordMapper attendanceRecordMapper;


    @Scheduled(fixedRate = 30000) // 每分钟检查一次
    public void checkDatabaseTime() {

    }

    @Scheduled(cron = "0 0 18 * * ?") // 每天18点执行
    public void AutoOffWork() {

        LocalDateTime checkInTime = LocalDateTime.now();
        LocalDate checkInDate = checkInTime.toLocalDate();

        // 获取当天所有考情记录
        List<AttendanceRecord> records = attendanceRecordMapper.selectList(new QueryWrapper<AttendanceRecord>().eq("date", checkInDate));
        if (records.isEmpty()) {
            return;
        }
        // 遍历所有考情记录
        for (AttendanceRecord record : records) {
            
            if (record.getCheckOutTime() == null) {
                // 未下班，自动下班
                attendanceService.updateCheckRecord(record.getAttendanceId());
            }


        }



    }


}