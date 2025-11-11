package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表格字段值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableFieldVO {
    private boolean root;
    private String fieldName;
}

