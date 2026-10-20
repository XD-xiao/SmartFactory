package com.example.smartfactoryservice.Domain.Vo.Production;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 大屏产线状态视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 运行中产线数 */
    private int runningCount;
    /** 已停机产线数 */
    private int stoppedCount;
    /** 产线详情列表 */
    private List<LineItem> lines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 产线序号 */
        private int id;
        /** 产线名称 */
        private String name;
        /** 运行状态：1=运行中, 0=已停机 */
        private int status;
        /** 本次运行开始时间（ISO格式，停机时为null） */
        private String startTime;
    }
}
