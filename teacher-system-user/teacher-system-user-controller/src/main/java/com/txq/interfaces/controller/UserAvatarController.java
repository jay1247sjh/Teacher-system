package com.txq.interfaces.controller;

import com.txq.application.entity.vo.UserAvatarVO;
import com.txq.application.service.IMetricsService;
import com.txq.application.service.IUserAvatarService;
import com.txq.common.annotation.ApiRequestMapping;
import com.txq.common.context.UserContext;
import com.txq.common.result.Response;
import com.txq.interfaces.dto.UploadAvatarRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户头像控制器
 */
@Slf4j
@ApiRequestMapping("/user/avatar")
@RequiredArgsConstructor
public class UserAvatarController {
    
    private final IUserAvatarService userAvatarService;
    private final IMetricsService metricsService;
    
    /**
     * 获取当前用户头像
     */
    @GetMapping
    public Response<UserAvatarVO> getMyAvatar() {
        String userId = UserContext.getUserId();
        UserAvatarVO avatar = userAvatarService.getUserAvatar(userId);
        return Response.success(avatar);
    }
    
    /**
     * 获取指定用户头像
     */
    @GetMapping("/{userId}")
    public Response<UserAvatarVO> getUserAvatar(@PathVariable String userId) {
        UserAvatarVO avatar = userAvatarService.getUserAvatar(userId);
        return Response.success(avatar);
    }
    
    /**
     * 上传头像文件
     */
    @PostMapping("/upload")
    public Response<UserAvatarVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String userId = UserContext.getUserId();
        log.info("用户 {} 上传头像文件", userId);
        try {
            UserAvatarVO avatar = userAvatarService.uploadAvatarFile(userId, file);
            metricsService.recordAvatarUpload();
            return Response.success(avatar);
        } catch (Exception e) {
            metricsService.recordAvatarUploadFailure();
            throw e;
        }
    }
    
    /**
     * 设置头像URL
     */
    @PostMapping("/url")
    public Response<UserAvatarVO> setAvatarUrl(@RequestBody UploadAvatarRequest request) {
        String userId = UserContext.getUserId();
        log.info("用户 {} 设置头像URL: {}", userId, request.getAvatarUrl());
        UserAvatarVO avatar = userAvatarService.setAvatarUrl(userId, request.getAvatarUrl());
        return Response.success(avatar);
    }
    
    /**
     * 删除头像
     */
    @DeleteMapping
    public Response<String> deleteAvatar() {
        String userId = UserContext.getUserId();
        log.info("用户 {} 删除头像", userId);
        userAvatarService.deleteAvatar(userId);
        return Response.success("删除成功");
    }
}

