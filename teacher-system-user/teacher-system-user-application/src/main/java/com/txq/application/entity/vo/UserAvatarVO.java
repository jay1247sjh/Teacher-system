package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户头像视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAvatarVO {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 头像路径（相对路径或完整URL）
     */
    private String avatarPath;
    
    /**
     * 完整的头像URL（前端可直接使用）
     */
    private String avatarUrl;
}

