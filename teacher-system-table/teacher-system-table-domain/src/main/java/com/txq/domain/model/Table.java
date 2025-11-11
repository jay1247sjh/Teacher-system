package com.txq.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

/**
 * 表格领域模型
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Table {
    // 表格ID
    private String id;

    // 表格全称
    private String tableFullName;

    // 表格别称
    private String tableAliasName;

    // 字段集合
    private List<TableField> tableFields;


    /**
     * 创建Table领域根
     */
    public static Table create(String tableFullName, String tableAliasName, List<TableField> tableFields) {
        String uuid = UUID.randomUUID().toString();
        return new Table(
                uuid,
                tableFullName,
                tableAliasName,
                tableFields
        );
    }
}
