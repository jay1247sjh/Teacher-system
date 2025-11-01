package com.txq.application.service.impl;

import com.txq.application.entity.vo.TableDataVO;
import com.txq.application.service.ITableDataService;
import com.txq.common.context.UserContext;
import com.txq.common.exception.BizException;
import com.txq.domain.infra.repository.TableDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.txq.domain.status.ErrorCode.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格数据服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TableDataServiceImpl implements ITableDataService {

    private final TableDataRepository tableDataRepository;

    @Override
    public Long saveTableData(Long id, Integer tableId, Map<String, Object> dataContent,
                             BigDecimal score, String reviewMaterial) {
        // 获取当前用户信息
        String userId = UserContext.getUserId();
        List<Integer> roleIds = UserContext.getRoleIds();
        
        log.info("保存表格数据 - 用户ID: {}, 角色列表: {}, 分数: {}", userId, roleIds, score);
        
        if (userId == null || roleIds == null || roleIds.isEmpty()) {
            throw new BizException(USER_NOT_LOGIN_ERROR_CODE, "用户未登录或权限信息缺失");
        }
        
        // 权限控制：只有管理员(roleId=2)和超级管理员(roleId=1)可以操作分数
        boolean isAdmin = roleIds.contains(1) || roleIds.contains(2);
        log.info("是否是管理员: {}, 角色列表: {}", isAdmin, roleIds);
        
        // 如果是普通成员(roleId=3)且提交了分数，则拒绝
        if (!isAdmin && score != null) {
            throw new BizException(NO_PERMISSION_SET_SCORE_ERROR_CODE, "普通成员无权限设置分数");
        }
        
        // 如果是更新操作，需要检查数据是否存在
        if (id != null) {
            Map<String, Object> existingData = tableDataRepository.findDataById(id);
            if (existingData == null) {
                throw new BizException(TABLE_DATA_NOT_EXIST_ERROR_CODE, "数据不存在");
            }
            
            // 如果是普通成员，保留原有分数（不允许修改）
            if (!isAdmin && existingData.get("score") != null) {
                score = (BigDecimal) existingData.get("score");
            }
        }
        
        log.info("保存表格数据，用户: {}, 角色: {}, 表格ID: {}, 数据ID: {}", 
                 userId, roleIds, tableId, id);
        
        return tableDataRepository.saveTableData(id, tableId, dataContent, score, reviewMaterial, userId);
    }

    @Override
    public List<TableDataVO> getTableData(Integer tableId) {
        List<Map<String, Object>> dataList = tableDataRepository.findDataByTableId(tableId);
        
        return dataList.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public TableDataVO getDataById(Long id) {
        Map<String, Object> data = tableDataRepository.findDataById(id);
        return data != null ? convertToVO(data) : null;
    }

    @Override
    public void deleteData(Long id) {
        // 可以在这里添加权限检查
        String userId = UserContext.getUserId();
        List<Integer> roleIds = UserContext.getRoleIds();
        
        if (userId == null || roleIds == null || roleIds.isEmpty()) {
            throw new BizException(USER_NOT_LOGIN_ERROR_CODE, "用户未登录或权限信息缺失");
        }
        
        // 检查数据是否存在
        Map<String, Object> data = tableDataRepository.findDataById(id);
        if (data == null) {
            throw new BizException(TABLE_DATA_NOT_EXIST_ERROR_CODE, "数据不存在");
        }
        
        // 普通成员只能删除自己创建的数据，管理员可以删除任何数据
        boolean isAdmin = roleIds.contains(1) || roleIds.contains(2);
        String createdBy = (String) data.get("createdBy");
        
        if (!isAdmin && !userId.equals(createdBy)) {
            throw new BizException(NO_PERMISSION_DELETE_DATA_ERROR_CODE, "无权删除他人创建的数据");
        }
        
        log.info("删除表格数据，用户: {}, 角色: {}, 数据ID: {}", userId, roleIds, id);
        
        tableDataRepository.deleteData(id);
    }

    @Override
    public void batchDeleteData(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        // 逐个检查权限
        for (Long id : ids) {
            deleteData(id);  // 复用单个删除的权限检查逻辑
        }
    }

    /**
     * 将Map转换为VO
     */
    @SuppressWarnings("unchecked")
    private TableDataVO convertToVO(Map<String, Object> map) {
        return TableDataVO.builder()
                .id(((Number) map.get("id")).longValue())
                .tableId((Integer) map.get("tableId"))
                .dataContent((Map<String, Object>) map.get("dataContent"))
                .score((BigDecimal) map.get("score"))
                .reviewMaterial((String) map.get("reviewMaterial"))
                .createdBy((String) map.get("createdBy"))
                .updatedBy((String) map.get("updatedBy"))
                .createdAt((LocalDateTime) map.get("createdAt"))
                .updatedAt((LocalDateTime) map.get("updatedAt"))
                .build();
    }
}

