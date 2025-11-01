package com.txq.application.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表格Query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableQuery {
    // 表格全称
    private String tableFullName;

    // 表格别称
    private String tableAliasName;

    // 字段集合
    private List<TableFieldQuery> tableFields;
}
