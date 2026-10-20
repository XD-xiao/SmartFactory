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
 * 产品出入库表
 */
@Data
@TableName("product_store")
public class ProductStore implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    private Integer action;

    @TableField("device_sn")
    private String deviceSn;

    @TableField("group_id")
    private Long groupId;

    @TableField("employee_id")
    private Long employeeId;

    @TableField("datetime")
    private LocalDateTime datetime;
}
