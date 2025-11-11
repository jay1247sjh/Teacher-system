package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 附件值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentVO {
    /**
     * 附件ID
     */
    private String attachmentId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件路径（相对路径）
     */
    private String filePath;
    
    /**
     * 文件URL（可直接访问）
     */
    private String fileUrl;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
}

