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

/**
 * 请假记录表
 */
@Data
@TableName("pm_leave_record")
public class LeaveRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "leave_id", type = IdType.AUTO)
    private Integer leaveId;

    @TableField("employee_id")
    private Integer employeeId;

    @TableField("leave_type")
    private Integer leaveType;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    private BigDecimal days;

    private String reason;

    private Integer status;
}
