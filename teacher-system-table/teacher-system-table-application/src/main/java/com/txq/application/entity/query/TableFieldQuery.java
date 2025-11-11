package com.txq.application.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表格字段Query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableFieldQuery {
    // 管理员操作字段
    private boolean root;

    // 字段名称
    private String fieldName;
}
