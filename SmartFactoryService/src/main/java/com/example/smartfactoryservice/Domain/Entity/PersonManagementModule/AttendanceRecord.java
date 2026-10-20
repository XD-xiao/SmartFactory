package com.example.smartfactoryservice.Domain.Entity.PersonManagementModule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录表
 */
@Data
@TableName("pm_attendance_record")
public class AttendanceRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "attendance_id", type = IdType.AUTO)
    private Integer attendanceId;

    @TableField("employee_id")
    private Integer employeeId;

    private LocalDate date;

    @TableField("check_in_time")
    private LocalDateTime checkInTime;

    @TableField("check_out_time")
    private LocalDateTime checkOutTime;

    private Integer status; //1:正常,2:迟到/早退

    @TableField("work_hours")
    private BigDecimal workHours;
}
