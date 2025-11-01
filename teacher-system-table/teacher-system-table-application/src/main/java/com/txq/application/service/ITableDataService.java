package com.txq.application.service;

import com.txq.application.entity.vo.TableDataVO;

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
     */
    Long saveTableData(Long id, Integer tableId, Map<String, Object> dataContent,
                      BigDecimal score, String reviewMaterial);

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
}

