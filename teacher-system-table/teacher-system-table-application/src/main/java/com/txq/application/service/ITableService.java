package com.txq.application.service;

import com.txq.application.entity.query.TableQuery;
import com.txq.application.entity.vo.TableFieldVO;
import com.txq.application.entity.vo.TableListItemVO;

import java.util.List;

/**
 * 表格信息接口
 */
public interface ITableService {
    /**
     * 创建表格
     */
    void createTable(TableQuery tableQuery);

    /**
     * 获取表格列表
     */
    List<TableListItemVO> getTableList();

    /**
     * 获取表格字段列表
     */
    List<TableFieldVO> getTableFields(Integer tableId);

    /**
     * 更新表格
     */
    void updateTable(Integer tableId, TableQuery tableQuery);

    /**
     * 删除表格
     */
    void deleteTable(Integer tableId);
}
