package com.txq.infrastructure.assembler;

import com.txq.domain.model.Table;
import com.txq.domain.model.TableField;
import com.txq.infrastructure.po.TableFieldPO;
import com.txq.infrastructure.po.TableMetaPO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表格转换器
 */
public class TableAssembler {

    /**
     * 领域模型转PO - 表格元信息
     */
    public static TableMetaPO toMetaPO(Table table, Integer tableId) {
        TableMetaPO po = new TableMetaPO();
        po.setId(tableId);
        po.setTableFullName(table.getTableFullName());
        po.setTableAliasName(table.getTableAliasName());
        // createTime 由数据库自动填充，不需要手动设置
        return po;
    }

    /**
     * 领域模型转PO - 表格字段
     */
    public static List<TableFieldPO> toFieldPOs(List<TableField> tableFields, Integer tableId) {
        return tableFields.stream()
                .map(field -> toFieldPO(field, tableId))
                .collect(Collectors.toList());
    }

    /**
     * 单个字段领域模型转PO
     */
    private static TableFieldPO toFieldPO(TableField tableField, Integer tableId) {
        TableFieldPO po = new TableFieldPO();
        po.setTableId(tableId);
        po.setRoot(tableField.isRoot());
        po.setFieldName(tableField.getFieldName());
        return po;
    }
}

