package com.txq.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txq.infrastructure.po.TableDataPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 表格数据Mapper
 */
@Mapper
public interface TableDataMapper extends BaseMapper<TableDataPO> {
    
    /**
     * 根据用户ID查询所有数据（关联表格信息）
     */
    @Select("SELECT td.id, td.table_id, td.user_id, td.data_content, td.score, " +
            "td.review_material, td.status, td.created_at, td.updated_at, tm.table_full_name " +
            "FROM table_data td " +
            "LEFT JOIN table_meta tm ON td.table_id = tm.id " +
            "WHERE td.user_id = #{userId} " +
            "ORDER BY td.created_at DESC")
    List<Map<String, Object>> selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据表格ID查询所有数据（关联用户信息和表格信息）
     * 用于管理员查看表格的用户得分统计和发送退回通知
     */
    @Select("SELECT td.id, td.table_id, td.user_id, td.submission_period, td.data_content, td.score, " +
            "td.review_material, td.reject_reason, td.status, td.created_at, td.updated_at, td.created_by, td.updated_by, " +
            "u.username, u.email, " +
            "tm.table_full_name " +
            "FROM table_data td " +
            "LEFT JOIN user u ON td.user_id = u.id " +
            "LEFT JOIN table_meta tm ON td.table_id = tm.id " +
            "WHERE td.table_id = #{tableId} " +
            "ORDER BY td.user_id, td.created_at DESC")
    List<Map<String, Object>> selectByTableIdWithUser(@Param("tableId") Integer tableId);
    
    /**
     * 统计全局数据（总数据量、待审核、已打分）
     * status: 0=暂存, 1=已提交(待审核), 2=已打分, 3=已退回
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as pendingCount, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as scoredCount " +
            "FROM table_data")
    Map<String, Object> selectGlobalStatistics();
    
    /**
     * 统计用户数据按状态分类
     * status: 0=暂存, 1=已提交(待审核), 2=已打分, 3=已退回
     */
    @Select("SELECT " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as pendingCount, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as scoredCount, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as rejectedCount " +
            "FROM table_data " +
            "WHERE user_id = #{userId}")
    Map<String, Object> selectUserStatisticsByStatus(@Param("userId") String userId);
}

