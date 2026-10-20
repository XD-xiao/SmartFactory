package com.example.smartfactoryservice.Domain.Entity.EduBoxModule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("edubox_sensor_config")
public class SensorConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String sn;

    private Long sensor;

    @TableField("sensor_name")
    private String sensorName;

    @TableField("sceneType")
    private Long sceneType;

    @TableField("valueType")
    private Boolean valueType;

    private Integer max;

    private Integer min;

    private Integer maxTh;

    private Integer midTh;

    private Integer minTh;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("delete_flag")
    private Boolean deleteFlag;
}
