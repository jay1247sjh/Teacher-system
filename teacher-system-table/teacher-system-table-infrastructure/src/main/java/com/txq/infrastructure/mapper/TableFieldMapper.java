package com.txq.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txq.infrastructure.po.TableFieldPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表格字段Mapper
 */
@Mapper
public interface TableFieldMapper extends BaseMapper<TableFieldPO> {

}

