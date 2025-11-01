package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 表格列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableListItemVO {
    /**
     * 表格ID
     */
    private Integer tableId;

    /**
     * 表格全称
     */
    private String tableFullName;

    /**
     * 表格别称
     */
    private String tableAliasName;

    /**
     * 字段数量
     */
    private Integer fieldCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

