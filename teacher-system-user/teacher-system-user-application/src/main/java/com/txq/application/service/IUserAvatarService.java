package com.txq.application.service;

import com.txq.application.entity.vo.UserAvatarVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户头像服务接口
 */
public interface IUserAvatarService {
    
    /**
     * 获取用户头像
     * @param userId 用户ID
     * @return 头像VO
     */
    UserAvatarVO getUserAvatar(String userId);
    
    /**
     * 上传头像文件
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像VO
     */
    UserAvatarVO uploadAvatarFile(String userId, MultipartFile file);
    
    /**
     * 设置头像URL
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 头像VO
     */
    UserAvatarVO setAvatarUrl(String userId, String avatarUrl);
    
    /**
     * 删除用户头像
     * @param userId 用户ID
     */
    void deleteAvatar(String userId);
}

