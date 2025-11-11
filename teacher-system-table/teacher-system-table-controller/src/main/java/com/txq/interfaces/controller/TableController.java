package com.txq.interfaces.controller;

import com.txq.application.entity.vo.TableDataVO;
import com.txq.application.entity.vo.TableListItemVO;
import com.txq.application.entity.vo.UserDataStatisticsVO;
import com.txq.application.service.ITableDataService;
import com.txq.application.service.ITableService;
import com.txq.common.annotation.ApiRequestMapping;
import com.txq.common.context.UserContext;
import com.txq.common.result.Response;
import com.txq.interfaces.converter.TableConverter;
import com.txq.interfaces.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表格控制器
 */
@Slf4j
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
                .map(vo -> new TableFieldDTO(vo.isRoot(), vo.getFieldName()))
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
                        .userId(vo.getUserId())
                        .submissionPeriod(vo.getSubmissionPeriod())
                        .dataContent(vo.getDataContent())
                        .score(vo.getScore())
                        .reviewMaterial(vo.getReviewMaterial())
                        .rejectReason(vo.getRejectReason())
                        .status(vo.getStatus())
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
                request.getUserId(),
                request.getSubmissionPeriod(),
                request.getDataContent(),
                request.getScore(),
                request.getReviewMaterial(),
                request.getStatus()
        );
        return Response.success(id);
    }

    /**
     * 暂存数据（普通用户功能）
     */
    @PostMapping("/data/draft")
    public Response<Long> saveDraft(@RequestBody SaveTableDataRequest request) {
        Long id = tableDataService.saveDraft(
                request.getId(),
                request.getTableId(),
                request.getUserId(),  // 传递userId参数
                request.getSubmissionPeriod(),
                request.getDataContent(),
                request.getReviewMaterial()
        );
        return Response.success(id);
    }

    /**
     * 提交数据（普通用户功能）
     */
    @PostMapping("/data/submit")
    public Response<Long> submitData(@RequestBody SaveTableDataRequest request) {
        Long id = tableDataService.submitData(
                request.getId(),
                request.getTableId(),
                request.getSubmissionPeriod(),
                request.getDataContent(),
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

    /**
     * 获取当前用户的数据统计
     */
    @GetMapping("/data/my-statistics")
    public Response<UserDataStatisticsVO> getMyDataStatistics() {
        String userId = UserContext.getUserId();
        log.info("获取用户数据统计，用户ID: {}", userId);
        UserDataStatisticsVO statistics = tableDataService.getUserDataStatistics(userId);
        return Response.success(statistics);
    }

    /**
     * 获取表格的用户得分统计（管理员功能）
     */
    @GetMapping("/{tableId}/score-statistics")
    public Response<com.txq.application.entity.vo.TableScoreStatisticsVO> getTableScoreStatistics(
            @PathVariable("tableId") Integer tableId) {
        log.info("获取表格用户得分统计，表格ID: {}", tableId);
        com.txq.application.entity.vo.TableScoreStatisticsVO statistics =
                tableDataService.getTableScoreStatistics(tableId);
        return Response.success(statistics);
    }

    /**
     * 退回数据（管理员功能）
     */
    @PostMapping("/data/reject")
    public Response<String> rejectData(@RequestBody RejectDataRequest request) {
        log.info("退回数据，数据ID: {}, 退回原因: {}", request.getId(), request.getRejectReason());
        tableDataService.rejectData(request.getId(), request.getRejectReason());
        return Response.success("退回成功");
    }

    /**
     * 获取全局数据统计（管理员功能）
     * 返回：totalCount(总数据量), pendingCount(待审核), scoredCount(已打分)
     */
    @GetMapping("/data/global-statistics")
    public Response<java.util.Map<String, Long>> getGlobalStatistics() {
        log.info("获取全局数据统计");
        java.util.Map<String, Long> statistics = tableDataService.getGlobalStatistics();
        return Response.success(statistics);
    }

    /**
     * 获取当前用户的数据按状态分类统计
     * 返回：pendingCount(待审核), scoredCount(已打分), rejectedCount(已退回)
     */
    @GetMapping("/data/my-status-statistics")
    public Response<java.util.Map<String, Long>> getMyStatusStatistics() {
        String userId = UserContext.getUserId();
        log.info("获取用户数据状态统计，用户ID: {}", userId);
        java.util.Map<String, Long> statistics = tableDataService.getMyStatusStatistics();
        return Response.success(statistics);
    }

    /**
     * 更新表格
     * 只有超级管理员(roleId=1)才有权限
     */
    @PutMapping("/update-table/{tableId}")
    // @RequiresRole(1)  // TODO: 临时注释，测试完成后需要恢复
    public Response<String> updateTable(
            @PathVariable("tableId") Integer tableId,
            @RequestBody TableDTO tableDTO) {
        tableService.updateTable(
                tableId,
                TableConverter.INSTANCE.toQuery(tableDTO)
        );
        return Response.success("更新成功");
    }

    /**
     * 删除表格
     * 只有超级管理员(roleId=1)才有权限
     */
    @DeleteMapping("/delete-table/{tableId}")
    // @RequiresRole(1)  // TODO: 临时注释，测试完成后需要恢复
    public Response<String> deleteTable(@PathVariable("tableId") Integer tableId) {
        tableService.deleteTable(tableId);
        return Response.success("删除成功");
    }
}
