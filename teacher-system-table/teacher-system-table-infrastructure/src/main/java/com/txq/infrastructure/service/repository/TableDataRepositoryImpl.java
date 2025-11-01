package com.txq.infrastructure.service.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.txq.domain.infra.repository.TableDataRepository;
import com.txq.infrastructure.mapper.TableDataMapper;
import com.txq.infrastructure.po.TableDataPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public Long saveTableData(Long id, Integer tableId, Map<String, Object> dataContent,
                             BigDecimal score, String reviewMaterial, String userId) {
        TableDataPO po = new TableDataPO();
        
        if (id != null) {
            // 更新操作
            po.setId(id);
            po.setUpdatedBy(userId);
        } else {
            // 新增操作
            po.setCreatedBy(userId);
        }
        
        po.setTableId(tableId);
        po.setDataContent(dataContent);
        po.setScore(score);
        po.setReviewMaterial(reviewMaterial);
        
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

    /**
     * 将PO转换为Map
     */
    private Map<String, Object> convertToMap(TableDataPO po) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", po.getId());
        map.put("tableId", po.getTableId());
        map.put("dataContent", po.getDataContent());
        map.put("score", po.getScore());
        map.put("reviewMaterial", po.getReviewMaterial());
        map.put("createdBy", po.getCreatedBy());
        map.put("updatedBy", po.getUpdatedBy());
        map.put("createdAt", po.getCreatedAt());
        map.put("updatedAt", po.getUpdatedAt());
        return map;
    }
}

