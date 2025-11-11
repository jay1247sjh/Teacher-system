package com.txq.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txq.infrastructure.po.UserRolePO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserRoleMapper extends BaseMapper<UserRolePO> {

}
