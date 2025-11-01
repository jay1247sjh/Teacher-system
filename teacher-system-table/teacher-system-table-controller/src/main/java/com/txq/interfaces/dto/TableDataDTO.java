package com.txq.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 表格数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableDataDTO {
    private Long id;
    private Integer tableId;
    private Map<String, Object> dataContent;  // 动态字段数据
    private BigDecimal score;                  // 分数
    private String reviewMaterial;             // 审核材料
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

