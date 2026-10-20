package com.example.smartfactoryservice.Domain.Entity.DeviceModule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("device_machine_failure")
public class DeviceMachineFailure implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String sn;

    private String reason;

    @TableField("failure_level")
    private Integer failureLevel;

    @TableField("failure_time")
    private String failureTime;

    private Integer status;

    @TableField("failure_recover_time")
    private String failureRecoverTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
