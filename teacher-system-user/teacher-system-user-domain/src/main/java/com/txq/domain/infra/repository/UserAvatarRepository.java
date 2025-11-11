package com.txq.domain.infra.repository;

/**
 * 用户头像仓储接口
 */
public interface UserAvatarRepository {
    
    /**
     * 根据用户ID获取头像路径
     * @param userId 用户ID
     * @return 头像路径（相对路径或完整URL），如果不存在返回null
     */
    String getAvatarPathByUserId(String userId);
    
    /**
     * 保存或更新用户头像
     * @param userId 用户ID
     * @param avatarPath 头像路径
     */
    void saveOrUpdateAvatar(String userId, String avatarPath);
    
    /**
     * 删除用户头像
     * @param userId 用户ID
     */
    void deleteAvatar(String userId);
}

