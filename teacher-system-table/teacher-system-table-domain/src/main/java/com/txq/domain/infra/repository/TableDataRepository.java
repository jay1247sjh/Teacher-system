package com.txq.domain.infra.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 表格数据持久化层接口
 */
public interface TableDataRepository {

    /**
     * 保存表格数据（新增或更新）
     */
    Long saveTableData(Long id, Integer tableId, Map<String, Object> dataContent, 
                      BigDecimal score, String reviewMaterial, String userId);

    /**
     * 根据表格ID获取所有数据
     */
    List<Map<String, Object>> findDataByTableId(Integer tableId);

    /**
     * 根据数据ID获取单条数据
     */
    Map<String, Object> findDataById(Long id);

    /**
     * 删除数据
     */
    void deleteData(Long id);

    /**
     * 批量删除数据
     */
    void batchDeleteData(List<Long> ids);
}

