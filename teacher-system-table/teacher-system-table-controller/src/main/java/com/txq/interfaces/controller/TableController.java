package com.txq.interfaces.controller;

import com.txq.application.entity.vo.TableListItemVO;
import com.txq.application.service.ITableService;
import com.txq.common.annotation.ApiRequestMapping;
import com.txq.common.annotation.RequiresRole;
import com.txq.common.result.Response;
import com.txq.interfaces.converter.TableConverter;
import com.txq.interfaces.dto.TableDTO;
import com.txq.interfaces.dto.TableFieldDTO;
import com.txq.interfaces.dto.TableListItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表格控制器
 */
@ApiRequestMapping("/table")
@RequiredArgsConstructor
public class TableController {

    private final ITableService tableService;

    /**
     * 创建表格
     * 只有超级管理员(roleId=1)才有权限
     */
    @PostMapping("/create-table")
    // @RequiresRole(1)  // TODO: 临时注释，测试完成后需要恢复
    public Response<String> createTable(@RequestBody TableDTO tableDTO) {
        tableService.createTable(
                TableConverter.INSTANCE.toQuery(tableDTO)
        );
        return Response.success("创建成功");
    }

    /**
     * 获取表格列表
     */
    @GetMapping("/list")
    public Response<List<TableListItemDTO>> getTableList() {
        List<TableListItemVO> tableList = tableService.getTableList();
        
        // 转换VO到DTO
        List<TableListItemDTO> dtoList = tableList.stream()
                .map(vo -> TableListItemDTO.builder()
                        .tableId(vo.getTableId())
                        .tableFullName(vo.getTableFullName())
                        .tableAliasName(vo.getTableAliasName())
                        .fieldCount(vo.getFieldCount())
                        .createTime(vo.getCreateTime())
                        .build())
                .collect(Collectors.toList());
        
        return Response.success(dtoList);
    }

    /**
     * 获取表格字段列表
     */
    @GetMapping("/{tableId}/fields")
    public Response<List<TableFieldDTO>> getTableFields(@PathVariable("tableId") Integer tableId) {
        List<com.txq.application.entity.vo.TableFieldVO> fields = tableService.getTableFields(tableId);
        
        // 转换VO到DTO
        List<TableFieldDTO> dtoList = fields.stream()
                .map(vo -> new TableFieldDTO(vo.isRoot(), vo.getFieldName(), vo.isCalc()))
                .collect(Collectors.toList());
        
        return Response.success(dtoList);
    }
}
