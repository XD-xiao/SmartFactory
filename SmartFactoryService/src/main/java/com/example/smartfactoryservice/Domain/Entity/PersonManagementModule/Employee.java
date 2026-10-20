package com.example.smartfactoryservice.Domain.Entity.PersonManagementModule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工表
 */
@Data
@TableName("pm_employee")
public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "employee_id", type = IdType.AUTO)
    private Integer employeeId;

    @TableField("employee_number")
    private String employeeNumber;

    private String name;

    private String password;

    private String gender;

    @TableField("birth_date")
    private LocalDate birthDate;

    private String phone;

    private String email;

    private String address;

    @TableField("id_card")
    private String idCard;

    @TableField("hire_date")
    private LocalDate hireDate;

    @TableField("department_id")
    private Integer departmentId;

    @TableField("position_id")
    private Integer positionId;

    @TableField("status")
    private Integer status; // 0:下班中 1:上班中

    @TableField("delete_flag")
    private boolean deleteFlag; //0：存在 1：已删除
}
