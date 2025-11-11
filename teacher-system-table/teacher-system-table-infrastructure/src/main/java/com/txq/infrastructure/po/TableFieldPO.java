package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 表格字段PO
 */
@Data
@Accessors(chain = true)
@TableName("table_field")
public class TableFieldPO {
    // 字段ID
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 关联表格ID
    private Integer tableId;

    // 是否为管理员操作字段
    private Boolean root;

    // 字段名称
    private String fieldName;
}

