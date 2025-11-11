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
    private String userId;
    private String submissionPeriod; // 提交时期（格式：YYYY-MM）
    private Map<String, Object> dataContent;
    private BigDecimal score;
    private String reviewMaterial;
    private String rejectReason; // 退回原因
    private Integer status; // 0=暂存，1=已提交，2=已打分，3=已退回
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

