package com.txq.interfaces.controller;

import com.txq.application.entity.vo.TableDataVO;
import com.txq.application.entity.vo.TableListItemVO;
import com.txq.application.service.ITableDataService;
import com.txq.application.service.ITableService;
import com.txq.common.annotation.ApiRequestMapping;
import com.txq.common.annotation.RequiresRole;
import com.txq.common.result.Response;
import com.txq.interfaces.converter.TableConverter;
import com.txq.interfaces.dto.*;
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
    private final ITableDataService tableDataService;

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

    /**
     * 获取表格数据列表
     */
    @GetMapping("/{tableId}/data")
    public Response<List<TableDataDTO>> getTableData(@PathVariable("tableId") Integer tableId) {
        List<TableDataVO> dataList = tableDataService.getTableData(tableId);
        
        // 转换VO到DTO
        List<TableDataDTO> dtoList = dataList.stream()
                .map(vo -> TableDataDTO.builder()
                        .id(vo.getId())
                        .tableId(vo.getTableId())
                        .dataContent(vo.getDataContent())
                        .score(vo.getScore())
                        .reviewMaterial(vo.getReviewMaterial())
                        .createdBy(vo.getCreatedBy())
                        .updatedBy(vo.getUpdatedBy())
                        .createdAt(vo.getCreatedAt())
                        .updatedAt(vo.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return Response.success(dtoList);
    }

    /**
     * 保存表格数据（新增或更新）
     */
    @PostMapping("/data/save")
    public Response<Long> saveTableData(@RequestBody SaveTableDataRequest request) {
        Long id = tableDataService.saveTableData(
                request.getId(),
                request.getTableId(),
                request.getDataContent(),
                request.getScore(),
                request.getReviewMaterial()
        );
        return Response.success(id);
    }

    /**
     * 删除表格数据
     */
    @DeleteMapping("/data/{id}")
    public Response<String> deleteTableData(@PathVariable("id") Long id) {
        tableDataService.deleteData(id);
        return Response.success("删除成功");
    }

    /**
     * 批量删除表格数据
     */
    @PostMapping("/data/batch-delete")
    public Response<String> batchDeleteTableData(@RequestBody List<Long> ids) {
        tableDataService.batchDeleteData(ids);
        return Response.success("批量删除成功");
    }
}
