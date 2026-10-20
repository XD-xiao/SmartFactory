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
@TableName("device_equipment_run_time")
public class DeviceEquipmentRunTime implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String sn;

    private Integer seqNo;

    private Integer action;

    @TableField("work_begin_time")
    private LocalDateTime workBeginTime;

    @TableField("work_end_time")
    private LocalDateTime workEndTime;

    @TableField("work_time")
    private Float workTime;
}
