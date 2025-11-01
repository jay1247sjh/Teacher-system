package com.txq.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 字段领域模型
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class TableField {
    // 管理员操作字段
    private boolean root;

    // 字段名称
    private String fieldName;

    // 是否为计算字段
    private boolean isCalc;

    /**
     * 创建TableField领域对象
     */
    public static TableField of(boolean root, String fieldName, boolean isCalc) {
        return new TableField(
                root,
                fieldName,
                isCalc
        );
    }
}
