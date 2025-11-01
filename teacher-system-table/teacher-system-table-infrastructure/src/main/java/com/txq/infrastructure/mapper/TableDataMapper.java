package com.txq.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txq.infrastructure.po.TableDataPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表格数据Mapper
 */
@Mapper
public interface TableDataMapper extends BaseMapper<TableDataPO> {
}

