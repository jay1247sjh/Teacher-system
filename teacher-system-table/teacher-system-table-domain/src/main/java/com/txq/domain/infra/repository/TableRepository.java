package com.txq.domain.infra.repository;

import com.txq.domain.model.Table;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取所有表格的基本信息
     * 返回的Map包含: tableId, tableFullName, tableAliasName, fieldCount, createTime
     */
    List<Map<String, Object>> findAllTables();

    /**
     * 获取表格的字段列表
     * 返回的Map包含: root, fieldName, isCalc
     */
    List<Map<String, Object>> findTableFields(Integer tableId);
}

