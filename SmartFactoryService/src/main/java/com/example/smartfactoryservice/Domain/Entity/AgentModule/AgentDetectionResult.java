package com.example.smartfactoryservice.Domain.Entity.AgentModule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("agent_detection_result")
public class AgentDetectionResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String status;

    @TableField("is_notified")
    private Integer isNotified;

    @TableField("data_start_time")
    private LocalDateTime dataStartTime;

    @TableField("data_end_time")
    private LocalDateTime dataEndTime;

    @TableField("result_report")
    private String resultReport;

    @TableField("analysis_start_time")
    private LocalDateTime analysisStartTime;

    @TableField("analysis_end_time")
    private LocalDateTime analysisEndTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
