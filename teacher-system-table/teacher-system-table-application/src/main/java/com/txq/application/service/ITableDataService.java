package com.txq.application.service;

import com.txq.application.entity.vo.TableDataVO;
import com.txq.application.entity.vo.TableScoreStatisticsVO;
import com.txq.application.entity.vo.UserDataStatisticsVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 表格数据服务接口
 */
public interface ITableDataService {

    /**
     * 保存表格数据（新增或更新）
     * 会根据用户角色进行权限控制
     * @param submissionPeriod 提交时期（格式：YYYY-MM）
     * @param status 数据状态：0=暂存，1=已提交，2=已打分
     */
    Long saveTableData(Long id, Integer tableId, String dataUserId, String submissionPeriod,
                      Map<String, Object> dataContent, BigDecimal score, String reviewMaterial, Integer status);

    /**
     * 暂存数据（普通用户功能）
     * 状态设置为0（暂存）
     * @param dataUserId 数据所属用户ID（管理员可指定，普通用户为null时使用当前用户）
     */
    Long saveDraft(Long id, Integer tableId, String dataUserId, String submissionPeriod, Map<String, Object> dataContent, String reviewMaterial);

    /**
     * 提交数据（普通用户功能）
     * 状态设置为1（已提交）
     */
    Long submitData(Long id, Integer tableId, String submissionPeriod, Map<String, Object> dataContent, String reviewMaterial);

    /**
     * 获取表格的所有数据
     */
    List<TableDataVO> getTableData(Integer tableId);

    /**
     * 获取单条数据
     */
    TableDataVO getDataById(Long id);

    /**
     * 删除数据
     */
    void deleteData(Long id);

    /**
     * 批量删除数据
     */
    void batchDeleteData(List<Long> ids);

    /**
     * 获取用户的数据统计
     * @param userId 用户ID
     * @return 用户数据统计
     */
    UserDataStatisticsVO getUserDataStatistics(String userId);

    /**
     * 获取表格的用户得分统计（管理员功能）
     * @param tableId 表格ID
     * @return 表格用户得分统计
     */
    TableScoreStatisticsVO getTableScoreStatistics(Integer tableId);

    /**
     * 退回数据（管理员功能）
     * 状态设置为3（已退回），并发送邮件通知用户
     * @param id 数据ID
     * @param rejectReason 退回原因
     */
    void rejectData(Long id, String rejectReason);

    /**
     * 获取全局数据统计（管理员功能）
     * @return Map包含: totalCount(总数据量), pendingCount(待审核), scoredCount(已打分)
     */
    Map<String, Long> getGlobalStatistics();

    /**
     * 获取当前用户的数据按状态分类统计
     * @return Map包含: pendingCount(待审核), scoredCount(已打分), rejectedCount(已退回)
     */
    Map<String, Long> getMyStatusStatistics();
}

