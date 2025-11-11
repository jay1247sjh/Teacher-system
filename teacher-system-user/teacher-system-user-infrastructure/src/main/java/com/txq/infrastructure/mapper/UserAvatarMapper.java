package com.txq.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txq.infrastructure.po.UserAvatarPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户头像Mapper
 */
@Mapper
public interface UserAvatarMapper extends BaseMapper<UserAvatarPO> {
}

