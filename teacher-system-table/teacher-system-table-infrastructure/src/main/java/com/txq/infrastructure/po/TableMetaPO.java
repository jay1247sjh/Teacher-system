package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 表格元信息PO
 */
@Data
@Accessors(chain = true)
@TableName("table_meta")
public class TableMetaPO {
    // 表格ID
    @TableId
    private Integer id;

    // 表格全称
    private String tableFullName;

    // 表格别称
    private String tableAliasName;

    // 创建时间（映射到数据库的 created_at 字段）
    @TableField("created_at")
    private LocalDateTime createTime;
}

