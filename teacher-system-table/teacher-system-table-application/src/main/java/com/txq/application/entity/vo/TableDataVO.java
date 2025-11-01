package com.txq.application.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 表格数据值对象
 */
@Data
@Builder
public class TableDataVO {
    private Long id;
    private Integer tableId;
    private Map<String, Object> dataContent;
    private BigDecimal score;
    private String reviewMaterial;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

