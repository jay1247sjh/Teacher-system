package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 表格数据PO
 */
@Data
@Accessors(chain = true)
@TableName(value = "table_data", autoResultMap = true)
public class TableDataPO {
    // 数据ID
    @TableId(type = IdType.AUTO)
    private Long id;

    // 表格ID
    private Integer tableId;

    // 所属用户工号
    private String userId;

    // 提交时期（格式：YYYY-MM）
    private String submissionPeriod;

    // 数据内容（JSON格式）
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> dataContent;

    // 分数（允许更新为null）
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal score;

    // 审核材料
    private String reviewMaterial;

    // 退回原因（允许更新为null）
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String rejectReason;

    // 数据状态：0=暂存（未提交），1=已提交，2=已打分（审核通过），3=已退回
    private Integer status;

    // 创建人工号
    private String createdBy;

    // 更新人工号
    private String updatedBy;

    // 创建时间
    @TableField("created_at")
    private LocalDateTime createdAt;

    // 更新时间
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

