package com.txq.infrastructure.service.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.txq.domain.infra.repository.UserAvatarRepository;
import com.txq.infrastructure.mapper.UserAvatarMapper;
import com.txq.infrastructure.po.UserAvatarPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 用户头像仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserAvatarRepositoryImpl implements UserAvatarRepository {
    
    private final UserAvatarMapper userAvatarMapper;
    
    @Override
    public String getAvatarPathByUserId(String userId) {
        LambdaQueryWrapper<UserAvatarPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAvatarPO::getUserId, userId);
        UserAvatarPO avatar = userAvatarMapper.selectOne(wrapper);
        return avatar != null ? avatar.getAvatarPath() : null;
    }
    
    @Override
    public void saveOrUpdateAvatar(String userId, String avatarPath) {
        LambdaQueryWrapper<UserAvatarPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAvatarPO::getUserId, userId);
        UserAvatarPO existingAvatar = userAvatarMapper.selectOne(wrapper);
        
        if (existingAvatar != null) {
            // 更新
            existingAvatar.setAvatarPath(avatarPath);
            userAvatarMapper.updateById(existingAvatar);
            log.info("更新用户头像: userId={}, avatarPath={}", userId, avatarPath);
        } else {
            // 新增
            UserAvatarPO newAvatar = new UserAvatarPO()
                    .setId(userId)  // 使用用户ID作为主键
                    .setUserId(userId)
                    .setAvatarPath(avatarPath);
            userAvatarMapper.insert(newAvatar);
            log.info("新增用户头像: userId={}, avatarPath={}", userId, avatarPath);
        }
    }
    
    @Override
    public void deleteAvatar(String userId) {
        LambdaQueryWrapper<UserAvatarPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAvatarPO::getUserId, userId);
        userAvatarMapper.delete(wrapper);
        log.info("删除用户头像: userId={}", userId);
    }
}

