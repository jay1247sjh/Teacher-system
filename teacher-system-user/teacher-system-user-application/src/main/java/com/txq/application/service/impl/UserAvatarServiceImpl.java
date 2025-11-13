package com.txq.application.service.impl;

import com.txq.application.entity.vo.UserAvatarVO;
import com.txq.application.service.IUserAvatarService;
import com.txq.common.exception.BizException;
import com.txq.domain.infra.repository.UserAvatarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.txq.domain.status.ErrorCode.*;

/**
 * 用户头像服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAvatarServiceImpl implements IUserAvatarService {

    private final UserAvatarRepository userAvatarRepository;

    @Value("${attachment.base-path}")
    private String attachmentBasePath;

    private final String projectRoot = System.getProperty("user.dir");

    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public UserAvatarVO getUserAvatar(String userId) {
        String avatarPath = userAvatarRepository.getAvatarPathByUserId(userId);

        if (avatarPath == null) {
            return new UserAvatarVO(userId, null, null);
        }

        // 判断是否是完整URL
        String avatarUrl;
        if (avatarPath.startsWith("http://") || avatarPath.startsWith("https://")) {
            avatarUrl = avatarPath;
        } else {
            // 相对路径，需要拼接
            avatarUrl = "/api/v1/attachments" + avatarPath;
        }

        return new UserAvatarVO(userId, avatarPath, avatarUrl);
    }

    @Override
    public UserAvatarVO uploadAvatarFile(String userId, MultipartFile file) {
        // 验证文件
        validateFile(file);

        // 构建存储路径
        String userDir = "users/" + userId;
        Path dirPath = Paths.get(projectRoot, attachmentBasePath, userDir);

        try {
            // 创建目录
            Files.createDirectories(dirPath);

            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 删除旧头像
            deleteOldAvatarFiles(dirPath);

            // 保存新头像（固定名称：avatar）
            String filename = "avatar" + extension;
            Path filePath = dirPath.resolve(filename);
            file.transferTo(filePath.toFile());

            log.info("用户 {} 头像上传成功: {}", userId, filePath);

            // 保存到数据库（相对路径）
            String avatarPath = "/" + userDir + "/" + filename;
            userAvatarRepository.saveOrUpdateAvatar(userId, avatarPath);

            // 返回VO
            return new UserAvatarVO(userId, avatarPath, "/api/v1/attachments" + avatarPath);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BizException(AVATAR_UPLOAD_ERROR_CODE, "文件上传失败");
        }
    }

    @Override
    public UserAvatarVO setAvatarUrl(String userId, String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            throw new BizException(AVATAR_URL_EMPTY_ERROR_CODE, "头像URL不能为空");
        }

        // 验证URL格式
        if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://")) {
            throw new BizException(AVATAR_URL_FORMAT_ERROR_CODE, "头像URL格式不正确，必须以http://或https://开头");
        }

        // 保存到数据库
        userAvatarRepository.saveOrUpdateAvatar(userId, avatarUrl);
        log.info("用户 {} 设置头像URL: {}", userId, avatarUrl);

        // 返回VO
        return new UserAvatarVO(userId, avatarUrl, avatarUrl);
    }

    @Override
    public void deleteAvatar(String userId) {
        // 获取当前头像路径
        String avatarPath = userAvatarRepository.getAvatarPathByUserId(userId);

        if (avatarPath != null && !avatarPath.startsWith("http://") && !avatarPath.startsWith("https://")) {
            // 如果是本地文件，删除物理文件
            Path filePath = Paths.get(projectRoot, attachmentBasePath + avatarPath);
            try {
                Files.deleteIfExists(filePath);
                log.info("删除用户 {} 的头像文件: {}", userId, filePath);
            } catch (IOException e) {
                log.error("删除头像文件失败", e);
            }
        }

        // 删除数据库记录
        userAvatarRepository.deleteAvatar(userId);
        log.info("删除用户 {} 的头像记录", userId);
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(AVATAR_FILE_EMPTY_ERROR_CODE, "文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException(AVATAR_FILE_SIZE_ERROR_CODE, "文件大小不能超过10MB");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BizException(AVATAR_FILE_EMPTY_ERROR_CODE, "文件名不能为空");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(extension)) {
            throw new BizException(AVATAR_FILE_FORMAT_ERROR_CODE, "不支持的文件格式，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    /**
     * 删除旧头像文件
     */
    private void deleteOldAvatarFiles(Path dirPath) {
        File dir = dirPath.toFile();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.startsWith("avatar"));
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        log.info("删除旧头像: {}", file.getAbsolutePath());
                    }
                }
            }
        }
    }
}

