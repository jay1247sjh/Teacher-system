package com.txq.application.service.impl;

import com.txq.application.entity.query.TableFieldQuery;
import com.txq.application.entity.query.TableQuery;
import com.txq.application.entity.vo.TableFieldVO;
import com.txq.application.entity.vo.TableListItemVO;
import com.txq.application.service.ITableService;
import com.txq.domain.infra.repository.TableDataRepository;
import com.txq.domain.infra.repository.TableRepository;
import com.txq.domain.model.Table;
import com.txq.domain.model.TableField;
import com.txq.domain.service.TableDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 表格应用服务
 * Application层负责业务编排和业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TableServiceImpl implements ITableService {

    private final TableDomainService tableDomainService;

    private final TableRepository tableRepository;
    
    private final TableDataRepository tableDataRepository;
    
    @Value("${attachment.base-path}")
    private String attachmentBasePath;
    
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
                        query.getFieldName()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 生成表格ID
     */
    private Integer generateTableId() {
        return Math.abs(random.nextInt());
    }

    /**
     * 获取表格列表
     */
    @Override
    public List<TableListItemVO> getTableList() {
        // 从仓储层获取表格列表
        List<Map<String, Object>> tables = tableRepository.findAllTables();
        
        // 转换为VO
        return tables.stream().map(table -> TableListItemVO.builder()
                .tableId((Integer) table.get("tableId"))
                .tableFullName((String) table.get("tableFullName"))
                .tableAliasName((String) table.get("tableAliasName"))
                .fieldCount((Integer) table.get("fieldCount"))
                .createTime((LocalDateTime) table.get("createTime"))
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * 获取表格字段列表
     */
    @Override
    public List<TableFieldVO> getTableFields(Integer tableId) {
        // 从仓储层获取字段列表
        List<Map<String, Object>> fields = tableRepository.findTableFields(tableId);
        
        // 转换为VO
        return fields.stream().map(field -> TableFieldVO.builder()
                .root((Boolean) field.get("root"))
                .fieldName((String) field.get("fieldName"))
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * 更新表格
     */
    @Override
    public void updateTable(Integer tableId, TableQuery tableQuery) {
        // 1. 检查表格是否存在
        if (!tableRepository.existsById(tableId)) {
            throw new RuntimeException("表格不存在");
        }

        // 2. 检查表格名称是否与其他表格重复
        if (tableRepository.existsByTableFullNameExcludeId(tableQuery.getTableFullName(), tableId)) {
            throw new RuntimeException("表格名称已存在");
        }

        // 3. 将Query转换为领域对象
        List<TableField> tableFields = convertToTableFields(tableQuery.getTableFields());
        Table table = Table.create(
                tableQuery.getTableFullName(),
                tableQuery.getTableAliasName(),
                tableFields
        );

        // 4. 更新表格信息
        tableRepository.updateTable(table, tableId);
    }

    /**
     * 删除表格
     */
    @Override
    public void deleteTable(Integer tableId) {
        // 1. 检查表格是否存在
        if (!tableRepository.existsById(tableId)) {
            throw new RuntimeException("表格不存在");
        }

        // 2. 删除该表格的所有附件文件
        deleteTableAttachments(tableId);

        // 3. 删除表格（会同时删除表格元信息、字段信息和数据记录）
        tableRepository.deleteTable(tableId);
        
        log.info("表格删除成功，tableId: {}", tableId);
    }
    
    /**
     * 删除表格的所有附件文件
     */
    private void deleteTableAttachments(Integer tableId) {
        try {
            // 查询该表格的所有数据记录
            List<Map<String, Object>> dataList = tableDataRepository.findDataByTableId(tableId);
            
            if (dataList == null || dataList.isEmpty()) {
                log.info("表格 {} 没有数据记录，无需删除附件", tableId);
                return;
            }
            
            int deletedCount = 0;
            int totalAttachments = 0;
            
            // 遍历所有数据记录，删除附件文件
            for (Map<String, Object> data : dataList) {
                String reviewMaterial = (String) data.get("review_material");
                
                if (reviewMaterial != null && !reviewMaterial.trim().isEmpty()) {
                    totalAttachments++;
                    
                    // 构建文件的完整路径
                    // reviewMaterial 格式: /table-data/{userId}/{attachmentId}_{filename}
                    Path filePath = Paths.get(attachmentBasePath + reviewMaterial);
                    
                    try {
                        if (Files.exists(filePath)) {
                            Files.delete(filePath);
                            deletedCount++;
                            log.info("删除附件文件: {}", filePath);
                        } else {
                            log.warn("附件文件不存在: {}", filePath);
                        }
                    } catch (IOException e) {
                        log.error("删除附件文件失败: {}", filePath, e);
                    }
                }
            }
            
            log.info("表格 {} 的附件删除完成，总计 {} 个附件，成功删除 {} 个", 
                    tableId, totalAttachments, deletedCount);
                    
        } catch (Exception e) {
            log.error("删除表格附件时出错，tableId: {}", tableId, e);
            // 不抛出异常，允许继续删除表格数据
        }
    }
}
