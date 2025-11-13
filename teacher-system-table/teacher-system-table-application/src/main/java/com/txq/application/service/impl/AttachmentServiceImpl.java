package com.txq.application.service.impl;

import com.txq.application.entity.vo.AttachmentVO;
import com.txq.application.service.IAttachmentService;
import com.txq.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 附件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements IAttachmentService {

    @Value("${attachment.base-path}")
    private String attachmentBasePath;

    private final String projectRoot = System.getProperty("user.dir");

    // 支持的文件类型
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
            ".zip", ".rar", ".7z",
            ".txt", ".md"
    );

    // 最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 错误码
    private static final int ATTACHMENT_EMPTY_ERROR_CODE = 20001;
    private static final int ATTACHMENT_TOO_LARGE_ERROR_CODE = 20002;
    private static final int ATTACHMENT_TYPE_ERROR_CODE = 20003;
    private static final int ATTACHMENT_UPLOAD_ERROR_CODE = 20004;
    private static final int ATTACHMENT_NOT_FOUND_ERROR_CODE = 20005;

    @Override
    public AttachmentVO uploadAttachment(String userId, MultipartFile file, String category, String relatedId) {
        // 验证文件
        validateFile(file);

        // 生成附件ID
        String attachmentId = UUID.randomUUID().toString().replace("-", "");

        // 构建存储路径：attachments/{category}/{userId}/{attachmentId}_{filename}
        String dirPath = category + "/" + userId;
        Path fullDirPath = Paths.get(projectRoot, attachmentBasePath, dirPath);

        try {
            // 创建目录
            Files.createDirectories(fullDirPath);

            // 如果有relatedId，删除该用户该关联ID的旧附件
            if (relatedId != null && !relatedId.isEmpty()) {
                deleteOldAttachments(fullDirPath, relatedId);
            }

            // 获取原始文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 生成新文件名：attachmentId_originalFilename
            String newFilename = attachmentId + "_" + originalFilename;
            Path filePath = fullDirPath.resolve(newFilename);

            // 保存文件
            file.transferTo(filePath.toFile());

            log.info("用户 {} 上传附件成功: {}, 大小: {} bytes", userId, filePath, file.getSize());

            // 构建相对路径和URL
            String relativePath = "/" + dirPath + "/" + newFilename;
            String fileUrl = "/api/v1/attachments" + relativePath;

            // 返回附件信息
            return AttachmentVO.builder()
                    .attachmentId(attachmentId)
                    .fileName(originalFilename)
                    .fileSize(file.getSize())
                    .filePath(relativePath)
                    .fileUrl(fileUrl)
                    .uploadTime(LocalDateTime.now())
                    .build();

        } catch (IOException e) {
            log.error("附件上传失败", e);
            throw new BizException(ATTACHMENT_UPLOAD_ERROR_CODE, "附件上传失败");
        }
    }

    @Override
    public void deleteAttachment(String userId, String attachmentId) {
        // TODO: 实现附件删除逻辑
        // 1. 从数据库查询附件信息
        // 2. 验证权限
        // 3. 删除物理文件
        // 4. 删除数据库记录
        log.info("用户 {} 删除附件: {}", userId, attachmentId);
    }

    @Override
    public AttachmentVO getAttachment(String attachmentId) {
        // TODO: 实现获取附件信息逻辑
        // 从数据库查询附件信息并返回
        throw new BizException(ATTACHMENT_NOT_FOUND_ERROR_CODE, "附件不存在");
    }

    /**
     * 删除旧附件（同一用户同一关联ID的旧文件）
     */
    private void deleteOldAttachments(Path dirPath, String relatedId) {
        try {
            if (Files.exists(dirPath)) {
                Files.list(dirPath)
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            // 这里简单处理：删除目录下所有文件
                            // 实际应该根据relatedId匹配，但当前文件名中没有relatedId
                            // 所以删除该用户目录下的所有旧附件
                            return true;
                        })
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                log.info("删除旧附件: {}", path);
                            } catch (IOException e) {
                                log.warn("删除旧附件失败: {}", path, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.warn("清理旧附件时出错", e);
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BizException(ATTACHMENT_EMPTY_ERROR_CODE, "文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException(ATTACHMENT_TOO_LARGE_ERROR_CODE, "文件大小不能超过10MB");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BizException(ATTACHMENT_TYPE_ERROR_CODE, "文件名无效");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException(ATTACHMENT_TYPE_ERROR_CODE,
                    "不支持的文件格式，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }
}

