package com.txq.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表格字段数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableFieldDTO {
    private Boolean root;
    private String fieldName;
}
