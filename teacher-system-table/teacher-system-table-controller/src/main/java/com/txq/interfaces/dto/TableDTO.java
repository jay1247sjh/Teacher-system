package com.txq.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表格DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableDTO {
    // 表格全称
    private String tableFullName;

    // 表格别称
    private String tableAliasName;

    // 字段集合
    private List<TableFieldDTO> tableFields;
}
