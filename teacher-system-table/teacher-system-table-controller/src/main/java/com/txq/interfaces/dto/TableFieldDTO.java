package com.txq.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表格字段DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableFieldDTO {
    // 管理员操作字段
    private boolean root;

    // 字段名称
    private String fieldName;

    // 是否为计算字段
    private boolean isCalc;
}
