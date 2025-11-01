package com.txq.interfaces.dto;

import lombok.Data;

/**
 * 上传头像请求（URL方式）
 */
@Data
public class UploadAvatarRequest {
    
    /**
     * 头像URL
     */
    private String avatarUrl;
}

