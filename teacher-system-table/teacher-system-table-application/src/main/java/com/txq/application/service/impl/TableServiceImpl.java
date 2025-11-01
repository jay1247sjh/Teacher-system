package com.txq.application.service.impl;

import com.txq.application.entity.query.TableFieldQuery;
import com.txq.application.entity.query.TableQuery;
import com.txq.application.service.ITableService;
import com.txq.domain.infra.repository.TableRepository;
import com.txq.domain.model.Table;
import com.txq.domain.model.TableField;
import com.txq.domain.service.TableDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 表格应用服务
 * Application层负责业务编排和业务逻辑
 */
@Service
@RequiredArgsConstructor
public class TableServiceImpl implements ITableService {

    private final TableDomainService tableDomainService;

    private final TableRepository tableRepository;
    private final Random random = new Random();

    /**
     * 创建表格
     */
    @Override
    public void createTable(TableQuery tableQuery) {
        // 1. 调用领域服务检查表格名称是否已存在
        tableDomainService.checkTableNameNotExist(tableQuery.getTableFullName());

        // 2. 生成表格ID
        Integer tableId = generateTableId();

        // 3. 将Query转换为领域对象
        List<TableField> tableFields = convertToTableFields(tableQuery.getTableFields());
        Table table = Table.create(
                tableQuery.getTableFullName(),
                tableQuery.getTableAliasName(),
                tableFields
        );

        // 4. 持久化表格信息
        tableRepository.saveTable(table, tableId);
    }

    /**
     * 将TableFieldQuery转换为TableField领域对象
     */
    private List<TableField> convertToTableFields(List<TableFieldQuery> fieldQueries) {
        return fieldQueries.stream()
                .map(query -> TableField.of(
                        query.isRoot(),
                        query.getFieldName(),
                        query.isCalc()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 生成表格ID
     */
    private Integer generateTableId() {
        return Math.abs(random.nextInt());
    }
}
