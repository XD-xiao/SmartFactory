package com.example.smartfactoryservice.Domain.Vo.Sensor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 大屏传感器模块视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 传感器列表 */
    private List<SensorItem> sensors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 传感器配置ID */
        private Long id;
        /** 传感器名称 */
        private String name;
        /** 部署场景编号 */
        private Long sceneType;
        /** 传感器记录历史（按时间升序） */
        private List<RecordPoint> records;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordPoint implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 记录值 */
        private Integer value;
        /** 记录状态：0=正常, 1=预警, 2=异常 */
        private Integer status;
        /** 记录时间（ISO格式字符串） */
        private String time;
    }
}
