package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户数据统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataStatisticsVO {
    
    /**
     * 总数据条数
     */
    private Integer totalCount;
    
    /**
     * 涉及表格数
     */
    private Integer tableCount;
    
    /**
     * 总分数
     */
    private Double totalScore;
    
    /**
     * 平均分数
     */
    private Double avgScore;
    
    /**
     * 按表格分组的数据
     */
    private List<DataByTableVO> dataByTable;
}

