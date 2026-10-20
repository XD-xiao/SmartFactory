package com.example.smartfactoryservice.Domain.Entity.PersonManagementModule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门表
 */
@Data
@TableName("pm_department")
public class Department implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "department_id", type = IdType.AUTO)
    private Integer departmentId;

    @TableField("department_name")
    private String departmentName;

    private Integer status;

    private String remark;
    @TableField("delete_flag")
    private boolean deleteFlag; //0：存在 1：已删除
}
