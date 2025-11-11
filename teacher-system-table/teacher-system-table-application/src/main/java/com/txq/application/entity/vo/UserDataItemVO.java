package com.txq.application.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户数据项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataItemVO {
    
    /**
     * 数据ID
     */
    private Long id;
    
    /**
     * 表格ID
     */
    private Integer tableId;
    
    /**
     * 表格名称
     */
    private String tableName;
    
    /**
     * 数据内容（JSON）
     */
    private Map<String, Object> dataContent;
    
    /**
     * 分数
     */
    private BigDecimal score;
    
    /**
     * 审核材料路径
     */
    private String reviewMaterial;
    
    /**
     * 数据状态：0=暂存，1=已提交，2=已打分
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
}

