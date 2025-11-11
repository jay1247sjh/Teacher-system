package com.txq.application.service;

import com.txq.application.entity.vo.AttachmentVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件服务接口
 */
public interface IAttachmentService {
    
    /**
     * 上传附件
     *
     * @param userId 用户ID
     * @param file 文件
     * @param category 附件分类
     * @param relatedId 关联ID
     * @return 附件信息
     */
    AttachmentVO uploadAttachment(String userId, MultipartFile file, String category, String relatedId);
    
    /**
     * 删除附件
     *
     * @param userId 用户ID
     * @param attachmentId 附件ID
     */
    void deleteAttachment(String userId, String attachmentId);
    
    /**
     * 获取附件信息
     *
     * @param attachmentId 附件ID
     * @return 附件信息
     */
    AttachmentVO getAttachment(String attachmentId);
}

