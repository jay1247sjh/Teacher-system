package com.txq.application.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.txq.application.entity.vo.*;
import com.txq.application.service.IEmailService;
import com.txq.application.service.ITableDataService;
import com.txq.common.context.UserContext;
import com.txq.common.exception.BizException;
import com.txq.domain.infra.repository.TableDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.txq.domain.status.ErrorCode.*;

/**
 * 表格数据服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TableDataServiceImpl implements ITableDataService {

    private final TableDataRepository tableDataRepository;

    private final IEmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Long saveTableData(Long id, Integer tableId, String dataUserId, String submissionPeriod,
                              Map<String, Object> dataContent, BigDecimal score, String reviewMaterial, Integer status) {
        // 获取当前操作用户信息
        String operatorId = UserContext.getUserId();
        List<Integer> roleIds = UserContext.getRoleIds();

        log.info("保存表格数据 - 操作用户ID: {}, 角色列表: {}, 数据所属用户: {}, 提交时期: {}, 分数: {}, 状态: {}",
                operatorId, roleIds, dataUserId, submissionPeriod, score, status);

        if (operatorId == null || roleIds == null || roleIds.isEmpty()) {
            throw new BizException(USER_NOT_LOGIN_ERROR_CODE, "用户未登录或权限信息缺失");
        }

        // 权限控制：只有管理员(roleId=2)和超级管理员(roleId=1)可以操作分数
        boolean isAdmin = roleIds.contains(1) || roleIds.contains(2);
        log.info("是否是管理员: {}, 角色列表: {}", isAdmin, roleIds);

        // 确定数据所属用户
        String finalDataUserId = dataUserId;
        if (!isAdmin) {
            // 普通成员：数据只能属于自己
            finalDataUserId = operatorId;
        } else if (dataUserId == null || dataUserId.isEmpty()) {
            // 管理员未指定用户：使用操作者ID
            finalDataUserId = operatorId;
        }

        // 如果是普通成员(roleId=3)且提交了分数，则拒绝
        if (!isAdmin && score != null) {
            throw new BizException(NO_PERMISSION_SET_SCORE_ERROR_CODE, "普通成员无权限设置分数");
        }

        // 确定最终状态
        Integer finalStatus = status;
        if (finalStatus == null) {
            finalStatus = 1; // 默认为已提交
        }

        // 如果管理员打分，自动设置状态为已打分
        if (isAdmin && score != null) {
            finalStatus = 2; // 已打分
        }

        // 如果是更新操作，需要检查数据是否存在
        if (id != null) {
            Map<String, Object> existingData = tableDataRepository.findDataById(id);
            if (existingData == null) {
                throw new BizException(TABLE_DATA_NOT_EXIST_ERROR_CODE, "数据不存在");
            }

            Integer existingStatus = (Integer) existingData.get("status");
            
            // 如果数据是被退回的（status=3），重新提交时清除分数，重置为待审核状态
            if (!isAdmin && existingStatus != null && existingStatus == 3) {
                score = null;  // 清除之前的分数
                finalStatus = 1;  // 重置为已提交状态
                log.info("普通成员重新提交被退回的数据，清除分数，状态重置为已提交");
            } 
            // 如果是普通成员且数据不是被退回的，保留原有分数（不允许修改）
            else if (!isAdmin && existingData.get("score") != null) {
                score = (BigDecimal) existingData.get("score");
            }

            // 如果数据已打分（status=2），普通用户不能修改
            if (!isAdmin && existingStatus != null && existingStatus == 2) {
                throw new BizException(SCORED_DATA_CANNOT_DELETE_ERROR_CODE, "已打分数据不允许修改");
            }
        }

        log.info("保存表格数据，操作用户: {}, 角色: {}, 数据所属用户: {}, 表格ID: {}, 数据ID: {}, 提交时期: {}, 最终状态: {}",
                operatorId, roleIds, finalDataUserId, tableId, id, submissionPeriod, finalStatus);

        return tableDataRepository.saveTableData(id, tableId, finalDataUserId, submissionPeriod, dataContent, score, reviewMaterial, finalStatus, operatorId);
    }

    @Override
    public Long saveDraft(Long id, Integer tableId, String dataUserId, String submissionPeriod, Map<String, Object> dataContent, String reviewMaterial) {
        String currentUserId = UserContext.getUserId();
        List<Integer> roleIds = UserContext.getRoleIds();
        
        // 判断是否为管理员
        boolean isAdmin = roleIds != null && (roleIds.contains(1) || roleIds.contains(2));
        
        // 如果是编辑已有数据且dataUserId为null，需要获取原数据的userId
        String finalDataUserId = dataUserId;
        if (id != null && finalDataUserId == null) {
            // 编辑已有数据，获取原数据的userId
            Map<String, Object> existingData = tableDataRepository.findDataById(id);
            if (existingData != null) {
                finalDataUserId = (String) existingData.get("userId");
            }
        }
        
        // 如果还是null，使用当前用户ID
        if (finalDataUserId == null) {
            finalDataUserId = currentUserId;
        }
        
        log.info("暂存数据，操作用户: {}, 数据所属用户: {}, 表格ID: {}, 数据ID: {}, 提交时期: {}", 
                currentUserId, finalDataUserId, tableId, id, submissionPeriod);

        // 状态设置为0（暂存/未提交）
        return saveTableData(id, tableId, finalDataUserId, submissionPeriod, dataContent, null, reviewMaterial, 0);
    }

    @Override
    public Long submitData(Long id, Integer tableId, String submissionPeriod, Map<String, Object> dataContent, String reviewMaterial) {
        String userId = UserContext.getUserId();
        log.info("提交数据，用户: {}, 表格ID: {}, 数据ID: {}, 提交时期: {}", userId, tableId, id, submissionPeriod);

        // 状态设置为1（已提交）
        return saveTableData(id, tableId, userId, submissionPeriod, dataContent, null, reviewMaterial, 1);
    }

    @Override
    public List<TableDataVO> getTableData(Integer tableId) {
        // 获取当前用户信息
        String userId = UserContext.getUserId();
        List<Integer> roleIds = UserContext.getRoleIds();

        if (userId == null || roleIds == null || roleIds.isEmpty()) {
            throw new BizException(USER_NOT_LOGIN_ERROR_CODE, "用户未登录或权限信息缺失");
        }

        // 判断是否为管理员（超级管理员或管理员）
        boolean isAdmin = roleIds.contains(1) || roleIds.contains(2);

        List<Map<String, Object>> dataList = tableDataRepository.findDataByTableId(tableId);

        // 如果是普通用户，只返回该用户自己的数据
        if (!isAdmin) {
            final String currentUserId = userId;
            dataList = dataList.stream()
                    .filter(data -> currentUserId.equals(data.get("userId")))
                    .collect(Collectors.toList());
        }

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

        // 检查数据状态：普通用户不能删除已打分的数据
        Integer status = (Integer) data.get("status");
        if (!isAdmin && status != null && status == 2) {
            throw new BizException(SCORED_DATA_CANNOT_DELETE_ERROR_CODE, "已打分数据不允许删除");
        }

        log.info("删除表格数据，用户: {}, 角色: {}, 数据ID: {}, 状态: {}", userId, roleIds, id, status);

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

    @Override
    public UserDataStatisticsVO getUserDataStatistics(String userId) {
        log.info("获取用户数据统计，用户ID: {}", userId);

        // 1. 查询用户的所有数据
        List<Map<String, Object>> userDataList = tableDataRepository.findByUserId(userId);

        if (userDataList.isEmpty()) {
            return UserDataStatisticsVO.builder()
                    .totalCount(0)
                    .tableCount(0)
                    .totalScore(0.0)
                    .avgScore(0.0)
                    .dataByTable(Collections.emptyList())
                    .build();
        }

        // 2. 计算统计数据
        int totalCount = userDataList.size();

        // 计算总分数
        double totalScore = userDataList.stream()
                .filter(data -> data.get("score") != null)
                .mapToDouble(data -> ((BigDecimal) data.get("score")).doubleValue())
                .sum();

        // 计算平均分数
        long scoredCount = userDataList.stream()
                .filter(data -> data.get("score") != null)
                .count();
        double avgScore = scoredCount > 0 ? totalScore / scoredCount : 0.0;

        // 3. 按表格分组
        Map<Integer, List<Map<String, Object>>> groupedData = userDataList.stream()
                .collect(Collectors.groupingBy(data -> convertToInteger(data.get("table_id"))));

        // 4. 构建按表格分组的数据
        List<DataByTableVO> dataByTable = groupedData.entrySet().stream()
                .map(entry -> {
                    Integer tableId = entry.getKey();
                    List<Map<String, Object>> dataList = entry.getValue();

                    // 获取表格名称（从第一条数据中获取）
                    String tableNameTemp = (String) dataList.get(0).get("table_full_name");
                    final String tableName = (tableNameTemp != null) ? tableNameTemp : "未知表格";

                    // 转换数据列表
                    List<UserDataItemVO> items = dataList.stream()
                            .map(data -> {
                                try {
                                    return UserDataItemVO.builder()
                                            .id(((Number) data.get("id")).longValue())
                                            .tableId(convertToInteger(data.get("table_id")))
                                            .tableName(tableName)
                                            .dataContent(parseDataContent((String) data.get("data_content")))
                                            .score((BigDecimal) data.get("score"))
                                            .reviewMaterial((String) data.get("review_material"))
                                            .status(convertToInteger(data.get("status")))
                                            .createdAt((LocalDateTime) data.get("created_at"))
                                            .updatedAt((LocalDateTime) data.get("updated_at"))
                                            .build();
                                } catch (Exception e) {
                                    log.error("转换数据项失败", e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    return DataByTableVO.builder()
                            .tableId(tableId)
                            .tableName(tableName)
                            .dataList(items)
                            .build();
                })
                .collect(Collectors.toList());

        // 5. 统计涉及的表格数
        int tableCount = groupedData.size();

        // 6. 构建返回结果
        return UserDataStatisticsVO.builder()
                .totalCount(totalCount)
                .tableCount(tableCount)
                .totalScore(totalScore)
                .avgScore(avgScore)
                .dataByTable(dataByTable)
                .build();
    }

    /**
     * 解析数据内容JSON
     */
    private Map<String, Object> parseDataContent(String dataContentJson) {
        try {
            return objectMapper.readValue(dataContentJson,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            log.error("解析数据内容失败: {}", dataContentJson, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 将对象转换为Integer
     * 处理 TINYINT(1) 被 MyBatis 映射为 Boolean 的问题
     */
    private Integer convertToInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法转换为Integer: {}", value);
            return null;
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
                .userId((String) map.get("userId"))
                .submissionPeriod((String) map.get("submissionPeriod"))
                .dataContent((Map<String, Object>) map.get("dataContent"))
                .score((BigDecimal) map.get("score"))
                .reviewMaterial((String) map.get("reviewMaterial"))
                .rejectReason((String) map.get("rejectReason"))
                .status(convertToInteger(map.get("status")))
                .createdBy((String) map.get("createdBy"))
                .updatedBy((String) map.get("updatedBy"))
                .createdAt((LocalDateTime) map.get("createdAt"))
                .updatedAt((LocalDateTime) map.get("updatedAt"))
                .build();
    }

    @Override
    public TableScoreStatisticsVO getTableScoreStatistics(Integer tableId) {
        log.info("获取表格用户得分统计，表格ID: {}", tableId);

        // 1. 查询表格的所有数据（关联用户信息）
        List<Map<String, Object>> allData = tableDataRepository.findByTableIdWithUser(tableId);

        if (allData.isEmpty()) {
            return TableScoreStatisticsVO.builder()
                    .tableId(tableId)
                    .tableName("")
                    .totalUsers(0)
                    .totalDataCount(0)
                    .totalScore(0.0)
                    .userScores(Collections.emptyList())
                    .build();
        }

        // 2. 按用户分组
        Map<String, List<Map<String, Object>>> groupedByUser = allData.stream()
                .collect(Collectors.groupingBy(data -> (String) data.get("user_id")));

        // 3. 计算每个用户的统计信息
        List<TableUserScoreVO> userScores = groupedByUser.entrySet().stream()
                .map(entry -> {
                    String userId = entry.getKey();
                    List<Map<String, Object>> userDataList = entry.getValue();

                    // 获取用户名（从第一条数据中获取）
                    String username = (String) userDataList.get(0).get("username");
                    if (username == null) {
                        username = "未知用户";
                    }

                    // 计算该用户的总分和平均分
                    double totalScore = userDataList.stream()
                            .filter(data -> data.get("score") != null)
                            .mapToDouble(data -> ((BigDecimal) data.get("score")).doubleValue())
                            .sum();

                    long scoredCount = userDataList.stream()
                            .filter(data -> data.get("score") != null)
                            .count();

                    double avgScore = scoredCount > 0 ? totalScore / scoredCount : 0.0;

                    // 转换数据列表
                    List<TableDataVO> dataVOList = userDataList.stream()
                            .map(data -> {
                                try {
                                    return TableDataVO.builder()
                                            .id(((Number) data.get("id")).longValue())
                                            .tableId(convertToInteger(data.get("table_id")))
                                            .userId((String) data.get("user_id"))
                                            .dataContent(parseDataContent((String) data.get("data_content")))
                                            .score((BigDecimal) data.get("score"))
                                            .reviewMaterial((String) data.get("review_material"))
                                            .status(convertToInteger(data.get("status")))
                                            .createdBy((String) data.get("created_by"))
                                            .updatedBy((String) data.get("updated_by"))
                                            .createdAt((LocalDateTime) data.get("created_at"))
                                            .updatedAt((LocalDateTime) data.get("updated_at"))
                                            .build();
                                } catch (Exception e) {
                                    log.error("转换数据项失败", e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    return TableUserScoreVO.builder()
                            .userId(userId)
                            .username(username)
                            .dataCount(userDataList.size())
                            .totalScore(totalScore)
                            .avgScore(avgScore)
                            .dataList(dataVOList)
                            .build();
                })
                .sorted(Comparator.comparing(TableUserScoreVO::getTotalScore).reversed())  // 按总分降序排列
                .collect(Collectors.toList());

        // 4. 计算整体统计
        int totalUsers = groupedByUser.size();
        int totalDataCount = allData.size();
        double totalScore = allData.stream()
                .filter(data -> data.get("score") != null)
                .mapToDouble(data -> ((BigDecimal) data.get("score")).doubleValue())
                .sum();

        // 5. 构建返回结果
        return TableScoreStatisticsVO.builder()
                .tableId(tableId)
                .tableName("")  // 表格名称可以从前端传入或从其他地方获取
                .totalUsers(totalUsers)
                .totalDataCount(totalDataCount)
                .totalScore(totalScore)
                .userScores(userScores)
                .build();
    }

    @Override
    public void rejectData(Long id, String rejectReason) {
        log.info("退回数据，数据ID: {}, 退回原因: {}", id, rejectReason);

        // 获取当前操作用户信息
        String operatorId = UserContext.getUserId();
        List<Integer> roleIds = UserContext.getRoleIds();

        if (operatorId == null || roleIds == null || roleIds.isEmpty()) {
            throw new BizException(USER_NOT_LOGIN_ERROR_CODE, "用户未登录或权限信息缺失");
        }

        // 权限控制：只有管理员可以退回数据
        boolean isAdmin = roleIds.contains(1) || roleIds.contains(2);
        if (!isAdmin) {
            throw new BizException(PERMISSION_DENIED_ERROR_CODE, "无权限退回数据");
        }

        // 1. 查询数据信息
        Map<String, Object> dataMap = tableDataRepository.findDataById(id);
        if (dataMap == null) {
            throw new BizException(PARAM_ERROR_CODE, "数据不存在");
        }

        // 2. 退回数据（更新状态和退回原因）
        tableDataRepository.rejectData(id, rejectReason, operatorId);

        // 3. 发送邮件通知
        try {
            // 获取数据所属用户ID
            String userId = (String) dataMap.get("userId");
            Integer tableId = (Integer) dataMap.get("tableId");
            String submissionPeriod = (String) dataMap.get("submissionPeriod");
            Map<String, Object> dataContent = (Map<String, Object>) dataMap.get("dataContent");

            // 从数据库查询用户和表格信息
            List<Map<String, Object>> userDataList = tableDataRepository.findByTableIdWithUser(tableId);

            // 找到对应用户的数据
            Map<String, Object> userData = userDataList.stream()
                    .filter(data -> userId.equals(data.get("user_id")))
                    .findFirst()
                    .orElse(null);

            if (userData != null) {
                String userEmail = (String) userData.get("email");
                String username = (String) userData.get("username");
                String tableName = (String) userData.get("table_full_name");

                if (userEmail != null && !userEmail.isEmpty()) {
                    emailService.sendRejectNotification(
                        userEmail, 
                        username, 
                        userId, 
                        tableName, 
                        submissionPeriod, 
                        dataContent, 
                        rejectReason, 
                        id
                    );
                    log.info("退回通知邮件已发送: userId={}, email={}, dataId={}", userId, userEmail, id);
                } else {
                    log.warn("用户邮箱为空，无法发送退回通知: userId={}", userId);
                }
            } else {
                log.warn("未找到用户数据，无法发送退回通知: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("发送退回通知邮件失败", e);
            // 邮件发送失败不影响业务主流程
        }

        log.info("数据退回成功，数据ID: {}", id);
    }

    @Override
    public Map<String, Long> getGlobalStatistics() {
        log.info("获取全局数据统计");
        
        // 获取当前操作用户信息
        String operatorId = UserContext.getUserId();
        List<Integer> roleIds = UserContext.getRoleIds();

        if (operatorId == null || roleIds == null || roleIds.isEmpty()) {
            throw new BizException(USER_NOT_LOGIN_ERROR_CODE, "用户未登录或权限信息缺失");
        }

        // 权限控制：只有管理员可以查看全局统计
        boolean isAdmin = roleIds.contains(1) || roleIds.contains(2);
        if (!isAdmin) {
            throw new BizException(PERMISSION_DENIED_ERROR_CODE, "无权限查看全局统计");
        }

        Map<String, Long> statistics = tableDataRepository.countGlobalStatistics();
        log.info("全局统计数据: {}", statistics);
        return statistics;
    }

    @Override
    public Map<String, Long> getMyStatusStatistics() {
        String userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(USER_NOT_LOGIN_ERROR_CODE, "用户未登录");
        }

        log.info("获取用户数据状态统计，用户ID: {}", userId);
        Map<String, Long> statistics = tableDataRepository.countUserStatisticsByStatus(userId);
        log.info("用户 {} 的状态统计: {}", userId, statistics);
        return statistics;
    }
}

