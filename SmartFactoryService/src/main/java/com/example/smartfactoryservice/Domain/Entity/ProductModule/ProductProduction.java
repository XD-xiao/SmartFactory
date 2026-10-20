package com.example.smartfactoryservice.Domain.Entity.ProductModule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品产量表
 */
@Data
@TableName("product_production")
public class ProductProduction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("planned_production")
    private Integer plannedProduction;

    @TableField("actual_production")
    private Integer actualProduction;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("store_num")
    private Integer storeNum;

    @TableField("qualified_num")
    private Integer qualifiedNum;

    @TableField("group_id")
    private Long groupId;

    @TableField("plan_name")
    private String planName;

}
