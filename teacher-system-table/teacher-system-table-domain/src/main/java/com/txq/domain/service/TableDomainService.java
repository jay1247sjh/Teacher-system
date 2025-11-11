package com.txq.domain.service;

import com.txq.common.exception.BizException;
import com.txq.domain.infra.repository.TableRepository;

import static com.txq.domain.status.ErrorCode.TABLE_EXIST_ERROR_CODE;

/**
 * 表格领域服务
 */
public class TableDomainService {

    private final TableRepository tableRepository;

    public TableDomainService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    /**
     * 检查表格名称是否已存在
     */
    public void checkTableNameNotExist(String tableFullName) {
        if (tableRepository.existsByTableFullName(tableFullName)) {
            throw new BizException(TABLE_EXIST_ERROR_CODE, "表格名称重复");
        }
    }
}

