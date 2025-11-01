package com.txq.domain.infra.repository;

import com.txq.domain.model.Table;

/**
 * 表格信息持久化层接口
 */
public interface TableRepository {

    /**
     * 保存表格信息（包含表格ID）
     */
    void saveTable(Table table, Integer tableId);

    /**
     * 根据表格全称判断是否存在
     */
    boolean existsByTableFullName(String tableFullName);
}

