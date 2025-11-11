package com.txq.infrastructure.service.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.txq.domain.infra.repository.TableDataRepository;
import com.txq.infrastructure.mapper.TableDataMapper;
import com.txq.infrastructure.po.TableDataPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格数据持久化实现
 */
@Repository
@RequiredArgsConstructor
public class TableDataRepositoryImpl implements TableDataRepository {

    private final TableDataMapper tableDataMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTableData(Long id, Integer tableId, String dataUserId, String submissionPeriod,
                             Map<String, Object> dataContent, BigDecimal score, String reviewMaterial, 
                             Integer status, String operatorId) {
        TableDataPO po = new TableDataPO();
        
        if (id != null) {
            // 更新操作
            po.setId(id);
            po.setUpdatedBy(operatorId);
        } else {
            // 新增操作
            po.setCreatedBy(operatorId);
        }
        
        po.setTableId(tableId);
        po.setUserId(dataUserId);  // 设置数据所属用户
        po.setSubmissionPeriod(submissionPeriod);  // 设置提交时期
        po.setDataContent(dataContent);
        po.setScore(score);
        po.setReviewMaterial(reviewMaterial);
        po.setStatus(status != null ? status : 1); // 默认为已提交
        
        // 如果是更新操作且从退回状态(3)变为已提交状态(1)，清除退回原因
        if (id != null && status == 1) {
            TableDataPO existingPO = tableDataMapper.selectById(id);
            if (existingPO != null && existingPO.getStatus() == 3) {
                po.setRejectReason(null);  // 清除退回原因
            }
        }
        
        if (id != null) {
            tableDataMapper.updateById(po);
        } else {
            tableDataMapper.insert(po);
        }
        
        return po.getId();
    }

    @Override
    public List<Map<String, Object>> findDataByTableId(Integer tableId) {
        LambdaQueryWrapper<TableDataPO> queryWrapper = new LambdaQueryWrapper<TableDataPO>()
                .eq(TableDataPO::getTableId, tableId)
                .orderByDesc(TableDataPO::getCreatedAt);
        
        List<TableDataPO> dataList = tableDataMapper.selectList(queryWrapper);
        
        return dataList.stream().map(this::convertToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> findDataById(Long id) {
        TableDataPO po = tableDataMapper.selectById(id);
        return po != null ? convertToMap(po) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteData(Long id) {
        tableDataMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteData(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            tableDataMapper.deleteBatchIds(ids);
        }
    }

    @Override
    public List<Map<String, Object>> findByUserId(String userId) {
        // 使用原生SQL查询，关联table表获取表格名称
        String sql = "SELECT td.id, td.table_id, td.user_id, td.data_content, td.score, " +
                     "td.review_material, td.created_at, td.updated_at, t.table_full_name " +
                     "FROM table_data td " +
                     "LEFT JOIN `table` t ON td.table_id = t.table_id " +
                     "WHERE td.user_id = #{userId} " +
                     "ORDER BY td.created_at DESC";
        
        return tableDataMapper.selectByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> findByTableIdWithUser(Integer tableId) {
        return tableDataMapper.selectByTableIdWithUser(tableId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectData(Long id, String rejectReason, String operatorId) {
        TableDataPO po = new TableDataPO();
        po.setId(id);
        po.setRejectReason(rejectReason);
        po.setStatus(3); // 状态设置为3（已退回）
        po.setScore(null); // 清除分数，退回后需要重新审核打分
        po.setUpdatedBy(operatorId);
        tableDataMapper.updateById(po);
    }

    @Override
    public Map<String, Long> countGlobalStatistics() {
        Map<String, Object> result = tableDataMapper.selectGlobalStatistics();
        Map<String, Long> statistics = new HashMap<>();
        
        // 将Object转换为Long，处理null值
        statistics.put("totalCount", convertToLong(result.get("totalCount")));
        statistics.put("pendingCount", convertToLong(result.get("pendingCount")));
        statistics.put("scoredCount", convertToLong(result.get("scoredCount")));
        
        return statistics;
    }

    @Override
    public Map<String, Long> countUserStatisticsByStatus(String userId) {
        Map<String, Object> result = tableDataMapper.selectUserStatisticsByStatus(userId);
        Map<String, Long> statistics = new HashMap<>();
        
        // 如果用户没有任何数据，返回全0的统计
        if (result == null) {
            statistics.put("pendingCount", 0L);
            statistics.put("scoredCount", 0L);
            statistics.put("rejectedCount", 0L);
            return statistics;
        }
        
        // 将Object转换为Long，处理null值
        statistics.put("pendingCount", convertToLong(result.get("pendingCount")));
        statistics.put("scoredCount", convertToLong(result.get("scoredCount")));
        statistics.put("rejectedCount", convertToLong(result.get("rejectedCount")));
        
        return statistics;
    }

    /**
     * 将Object转换为Long，处理null值和不同数字类型
     */
    private Long convertToLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * 将PO转换为Map
     */
    private Map<String, Object> convertToMap(TableDataPO po) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", po.getId());
        map.put("tableId", po.getTableId());
        map.put("userId", po.getUserId());
        map.put("submissionPeriod", po.getSubmissionPeriod());
        map.put("dataContent", po.getDataContent());
        map.put("score", po.getScore());
        map.put("reviewMaterial", po.getReviewMaterial());
        map.put("rejectReason", po.getRejectReason());
        map.put("status", po.getStatus());
        map.put("createdBy", po.getCreatedBy());
        map.put("updatedBy", po.getUpdatedBy());
        map.put("createdAt", po.getCreatedAt());
        map.put("updatedAt", po.getUpdatedAt());
        return map;
    }
}

