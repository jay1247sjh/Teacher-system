package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表格用户得分统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableUserScoreVO {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 该用户在此表的数据条数
     */
    private Integer dataCount;
    
    /**
     * 该用户在此表的总分
     */
    private Double totalScore;
    
    /**
     * 该用户在此表的平均分
     */
    private Double avgScore;
    
    /**
     * 该用户在此表的数据列表
     */
    private List<TableDataVO> dataList;
}

