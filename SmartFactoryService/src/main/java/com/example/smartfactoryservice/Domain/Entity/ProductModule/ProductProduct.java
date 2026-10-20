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
 * 产品表
 */
@Data
@TableName("product_product")
public class ProductProduct implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("bar_code")
    private String barCode;

    @TableField("type_code")
    private String typeCode;

    @TableField("model_code")
    private String modelCode;

    @TableField("product_code")
    private String productCode;

    @TableField("pic_id")
    private Long picId;

    @TableField("device_sn")
    private String deviceSn;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("delete_flag")
    private boolean deleteFlag; //0：存在 1：已删除
}
