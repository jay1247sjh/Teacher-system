package com.txq.interfaces.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 保存表格数据请求
 */
@Data
public class SaveTableDataRequest {
    private Long id;  // 如果有ID则是更新，否则是新增
    private Integer tableId;
    private String userId;                     // 所属用户工号
    private String submissionPeriod;           // 提交时期（格式：YYYY-MM）
    private Map<String, Object> dataContent;  // 动态字段数据
    private BigDecimal score;                  // 分数（普通成员不能提交）
    private String reviewMaterial;             // 审核材料
    private Integer status;                    // 数据状态：0=暂存，1=已提交，2=已打分
}

