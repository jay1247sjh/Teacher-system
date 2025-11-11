package com.txq.application.service;

import java.util.function.Supplier;

/**
 * 指标服务接口
 * 用于在应用层收集业务指标
 */
public interface IMetricsService {

    // ========== 登录相关 ==========
    
    /**
     * 记录登录尝试
     */
    void recordLoginAttempt();
    
    /**
     * 记录登录成功
     */
    void recordLoginSuccess(String userId);
    
    /**
     * 记录登录失败
     */
    void recordLoginFailure();
    
    /**
     * 记录登录耗时
     * @param loginAction 登录操作
     * @return 登录结果
     */
    <T> T recordLoginDuration(Supplier<T> loginAction);
    
    // ========== 注册相关 ==========
    
    /**
     * 记录注册尝试
     */
    void recordRegisterAttempt();
    
    /**
     * 记录注册成功
     */
    void recordRegisterSuccess();
    
    /**
     * 记录注册失败
     */
    void recordRegisterFailure();
    
    /**
     * 记录验证码发送
     */
    void recordVerificationCodeSent();
    
    // ========== 用户管理相关 ==========
    
    /**
     * 记录用户创建
     */
    void recordUserCreated();
    
    /**
     * 记录用户更新
     */
    void recordUserUpdated();
    
    /**
     * 记录用户删除
     */
    void recordUserDeleted();
    
    /**
     * 记录头像上传
     */
    void recordAvatarUpload();
    
    /**
     * 记录头像上传失败
     */
    void recordAvatarUploadFailure();
    
    // ========== 在线用户管理 ==========
    
    /**
     * 添加在线用户
     */
    void addOnlineUser(String userId);
    
    /**
     * 移除在线用户
     */
    void removeOnlineUser(String userId);
    
    /**
     * 刷新用户会话
     */
    void refreshUserSession(String userId);
}

