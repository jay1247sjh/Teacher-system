package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户头像持久化对象
 */
@Data
@Accessors(chain = true)
@TableName("user_avatar")
public class UserAvatarPO {
    
    /**
     * 头像ID（使用用户工号）
     */
    @TableId(type = IdType.INPUT)
    private String id;
    
    /**
     * 关联用户
     */
    private String userId;
    
    /**
     * 存放路径（相对路径或完整URL）
     */
    private String avatarPath;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

