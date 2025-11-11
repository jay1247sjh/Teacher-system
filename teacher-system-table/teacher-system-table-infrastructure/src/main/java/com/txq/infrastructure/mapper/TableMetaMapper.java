package com.txq.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txq.infrastructure.po.TableMetaPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表格元信息Mapper
 */
@Mapper
public interface TableMetaMapper extends BaseMapper<TableMetaPO> {

}

