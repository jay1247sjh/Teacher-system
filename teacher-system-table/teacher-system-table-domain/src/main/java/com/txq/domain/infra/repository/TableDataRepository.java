package com.txq.domain.infra.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 表格数据持久化层接口
 */
public interface TableDataRepository {

    /**
     * 保存表格数据（新增或更新）
     * @param dataUserId 数据所属用户ID
     * @param submissionPeriod 提交时期（格式：YYYY-MM）
     * @param status 数据状态：0=暂存，1=已提交，2=已打分
     * @param operatorId 操作者ID（创建人/更新人）
     */
    Long saveTableData(Long id, Integer tableId, String dataUserId, String submissionPeriod,
                      Map<String, Object> dataContent, BigDecimal score, String reviewMaterial, 
                      Integer status, String operatorId);

    /**
     * 根据表格ID获取所有数据
     */
    List<Map<String, Object>> findDataByTableId(Integer tableId);

    /**
     * 根据数据ID获取单条数据
     */
    Map<String, Object> findDataById(Long id);

    /**
     * 删除数据
     */
    void deleteData(Long id);

    /**
     * 批量删除数据
     */
    void batchDeleteData(List<Long> ids);

    /**
     * 根据用户ID查询所有数据
     * @param userId 用户ID
     * @return 数据列表（包含表格信息）
     */
    List<Map<String, Object>> findByUserId(String userId);
    
    /**
     * 根据表格ID查询所有数据（关联用户信息）
     * 用于管理员查看表格的用户得分统计
     * @param tableId 表格ID
     * @return 数据列表（包含用户信息）
     */
    List<Map<String, Object>> findByTableIdWithUser(Integer tableId);

    /**
     * 退回数据
     * @param id 数据ID
     * @param rejectReason 退回原因
     * @param operatorId 操作人ID
     */
    void rejectData(Long id, String rejectReason, String operatorId);

    /**
     * 统计全局数据
     * @return Map包含: totalCount(总数据量), pendingCount(待审核), scoredCount(已打分)
     */
    Map<String, Long> countGlobalStatistics();

    /**
     * 统计用户数据按状态分类
     * @param userId 用户ID
     * @return Map包含: pendingCount(待审核), scoredCount(已打分), rejectedCount(已退回)
     */
    Map<String, Long> countUserStatisticsByStatus(String userId);
}

