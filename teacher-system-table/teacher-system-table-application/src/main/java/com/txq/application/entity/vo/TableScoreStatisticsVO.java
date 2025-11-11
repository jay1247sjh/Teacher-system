package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表格得分统计VO（管理员视角）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableScoreStatisticsVO {
    
    /**
     * 表格ID
     */
    private Integer tableId;
    
    /**
     * 表格名称
     */
    private String tableName;
    
    /**
     * 总用户数
     */
    private Integer totalUsers;
    
    /**
     * 总数据条数
     */
    private Integer totalDataCount;
    
    /**
     * 所有用户总分
     */
    private Double totalScore;
    
    /**
     * 用户得分列表
     */
    private List<TableUserScoreVO> userScores;
}

