package com.txq.infrastructure.service.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.txq.domain.infra.repository.TableRepository;
import com.txq.domain.model.Table;
import com.txq.infrastructure.assembler.TableAssembler;
import com.txq.infrastructure.mapper.TableFieldMapper;
import com.txq.infrastructure.mapper.TableMetaMapper;
import com.txq.infrastructure.po.TableFieldPO;
import com.txq.infrastructure.po.TableMetaPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格数据持久层
 * Infrastructure层只提供最基础的数据持久化服务，不关心业务逻辑
 */
@Repository
@RequiredArgsConstructor
public class TableRepositoryImpl implements TableRepository {

    private final TableMetaMapper tableMetaMapper;

    private final TableFieldMapper tableFieldMapper;

    /**
     * 保存表格信息（包含元信息和字段信息）
     * 只负责数据持久化，tableId由Application层传入
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTable(Table table, Integer tableId) {
        // 保存表格元信息
        TableMetaPO metaPO = TableAssembler.toMetaPO(table, tableId);
        tableMetaMapper.insert(metaPO);

        // 保存表格字段信息
        List<TableFieldPO> fieldPOs = TableAssembler.toFieldPOs(table.getTableFields(), tableId);
        for (TableFieldPO fieldPO : fieldPOs) {
            tableFieldMapper.insert(fieldPO);
        }
    }

    /**
     * 根据表格全称判断是否存在
     */
    @Override
    public boolean existsByTableFullName(String tableFullName) {
        LambdaQueryWrapper<TableMetaPO> queryWrapper = new LambdaQueryWrapper<TableMetaPO>()
                .eq(TableMetaPO::getTableFullName, tableFullName);
        return tableMetaMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 获取所有表格的基本信息
     */
    @Override
    public List<Map<String, Object>> findAllTables() {
        // 查询所有表格元信息
        List<TableMetaPO> metaPOs = tableMetaMapper.selectList(null);
        
        // 转换为Map格式
        return metaPOs.stream().map(metaPO -> {
            // 查询该表格的字段数量
            LambdaQueryWrapper<TableFieldPO> fieldQuery = new LambdaQueryWrapper<TableFieldPO>()
                    .eq(TableFieldPO::getTableId, metaPO.getId());
            long fieldCount = tableFieldMapper.selectCount(fieldQuery);
            
            // 构建返回数据
            Map<String, Object> tableInfo = new HashMap<>();
            tableInfo.put("tableId", metaPO.getId());
            tableInfo.put("tableFullName", metaPO.getTableFullName());
            tableInfo.put("tableAliasName", metaPO.getTableAliasName());
            tableInfo.put("fieldCount", (int) fieldCount);
            tableInfo.put("createTime", metaPO.getCreateTime());
            
            return tableInfo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取表格的字段列表
     */
    @Override
    public List<Map<String, Object>> findTableFields(Integer tableId) {
        // 查询指定表格的所有字段
        LambdaQueryWrapper<TableFieldPO> queryWrapper = new LambdaQueryWrapper<TableFieldPO>()
                .eq(TableFieldPO::getTableId, tableId);
        List<TableFieldPO> fieldPOs = tableFieldMapper.selectList(queryWrapper);
        
        // 转换为Map格式
        return fieldPOs.stream().map(fieldPO -> {
            Map<String, Object> fieldInfo = new HashMap<>();
            fieldInfo.put("root", fieldPO.getRoot());
            fieldInfo.put("fieldName", fieldPO.getFieldName());
            fieldInfo.put("isCalc", fieldPO.getIsCalc());
            return fieldInfo;
        }).collect(Collectors.toList());
    }
}

