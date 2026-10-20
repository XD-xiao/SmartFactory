package com.example.smartfactoryservice.Domain.Entity.PersonManagementModule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 职位表
 */
@Data
@TableName("pm_position")
public class Position implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "position_id", type = IdType.AUTO)
    private Integer positionId;

    @TableField("position_name")
    private String positionName;

    private String remark;

    @TableField("delete_flag")
    private boolean deleteFlag; //0：存在 1：已删除
}
