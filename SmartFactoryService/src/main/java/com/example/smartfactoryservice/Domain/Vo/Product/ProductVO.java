package com.example.smartfactoryservice.Domain.Vo.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 大屏产品模块视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 总入库数 */
    private int totalInbound;
    /** 总合格数 */
    private int totalQualified;
    /** 总产量 */
    private int totalOutput;
    /** 总计划产量 */
    private int totalPlanned;
    /** 合格率 */
    private double qualifyRate;
    /** 近30日每日入库量 */
    private List<Integer> inboundSeries;
    /** 近30日每日出库量 */
    private List<Integer> outboundSeries;
    /** 生产计划列表 */
    private List<PlanItem> plans;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 计划ID */
        private long id;
        /** 计划名称 */
        private String name;
        /** 计划产量 */
        private int planQty;
        /** 实际产量 */
        private int output;
        /** 合格数 */
        private int qualified;
        /** 入库数 */
        private int inbound;
        /** 完成率 */
        private int rate;
    }
}
