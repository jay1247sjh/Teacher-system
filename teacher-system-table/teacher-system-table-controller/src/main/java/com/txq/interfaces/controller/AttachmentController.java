package com.txq.interfaces.controller;

import com.txq.application.entity.vo.AttachmentVO;
import com.txq.application.service.IAttachmentService;
import com.txq.common.annotation.ApiRequestMapping;
import com.txq.common.context.UserContext;
import com.txq.common.result.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件控制器
 */
@Slf4j
@ApiRequestMapping("/table/attachment")
@RequiredArgsConstructor
public class AttachmentController {
    
    private final IAttachmentService attachmentService;
    
    /**
     * 上传附件
     */
    @PostMapping("/upload")
    public Response<AttachmentVO> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "relatedId", required = false) String relatedId) {
        String userId = UserContext.getUserId();
        log.info("用户 {} 上传附件, category: {}, relatedId: {}", userId, category, relatedId);
        
        AttachmentVO attachment = attachmentService.uploadAttachment(userId, file, category, relatedId);
        return Response.success(attachment);
    }
    
    /**
     * 删除附件
     */
    @DeleteMapping("/{attachmentId}")
    public Response<String> deleteAttachment(@PathVariable String attachmentId) {
        String userId = UserContext.getUserId();
        log.info("用户 {} 删除附件: {}", userId, attachmentId);
        
        attachmentService.deleteAttachment(userId, attachmentId);
        return Response.success("删除成功");
    }
    
    /**
     * 获取附件信息
     */
    @GetMapping("/{attachmentId}")
    public Response<AttachmentVO> getAttachment(@PathVariable String attachmentId) {
        AttachmentVO attachment = attachmentService.getAttachment(attachmentId);
        return Response.success(attachment);
    }
}

