package com.txq.application.service;

import com.txq.application.entity.query.TableQuery;

/**
 * 表格信息接口
 */
public interface ITableService {
    /**
     * 创建表格
     */
    void createTable(TableQuery tableQuery);
}
