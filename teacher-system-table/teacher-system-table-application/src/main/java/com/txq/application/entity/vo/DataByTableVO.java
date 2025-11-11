package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 按表格分组的数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataByTableVO {
    
    /**
     * 表格ID
     */
    private Integer tableId;
    
    /**
     * 表格名称
     */
    private String tableName;
    
    /**
     * 数据列表
     */
    private List<UserDataItemVO> dataList;
}

