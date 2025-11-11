package com.txq.interfaces.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 业务指标配置类
 * 用于收集和暴露用户相关的业务指标
 */
@Getter
@Component
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    
    // ========== 登录相关指标 ==========
    
    /** 登录尝试总数计数器 */
    private final Counter loginAttemptCounter;
    
    /** 登录成功总数计数器 */
    private final Counter loginSuccessCounter;
    
    /** 登录失败总数计数器 */
    private final Counter loginFailureCounter;
    
    /** 登录耗时计时器 */
    private final Timer loginTimer;
    
    /** 当前在线用户数（仪表盘） */
    private final AtomicInteger onlineUserCount;
    
    // ========== 注册相关指标 ==========
    
    /** 注册尝试总数计数器 */
    private final Counter registerAttemptCounter;
    
    /** 注册成功总数计数器 */
    private final Counter registerSuccessCounter;
    
    /** 注册失败总数计数器 */
    private final Counter registerFailureCounter;
    
    /** 验证码发送总数计数器 */
    private final Counter verificationCodeSentCounter;
    
    // ========== 用户管理相关指标 ==========
    
    /** 用户创建总数计数器 */
    private final Counter userCreatedCounter;
    
    /** 用户更新总数计数器 */
    private final Counter userUpdatedCounter;
    
    /** 用户删除总数计数器 */
    private final Counter userDeletedCounter;
    
    /** 头像上传总数计数器 */
    private final Counter avatarUploadCounter;
    
    /** 头像上传失败总数计数器 */
    private final Counter avatarUploadFailureCounter;
    
    // ========== 会话管理 ==========
    
    /** 记录在线用户ID（用于统计在线人数） */
    private final ConcurrentHashMap<String, Long> onlineUsers;
    
    /** 会话超时时长（30分钟，与JWT过期时间一致） */
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.onlineUsers = new ConcurrentHashMap<>();
        this.onlineUserCount = new AtomicInteger(0);
        
        // 初始化登录相关指标
        this.loginAttemptCounter = Counter.builder("user.login.attempt")
                .description("用户登录尝试总数")
                .tag("type", "authentication")
                .register(meterRegistry);
        
        this.loginSuccessCounter = Counter.builder("user.login.success")
                .description("用户登录成功总数")
                .tag("type", "authentication")
                .register(meterRegistry);
        
        this.loginFailureCounter = Counter.builder("user.login.failure")
                .description("用户登录失败总数")
                .tag("type", "authentication")
                .register(meterRegistry);
        
        this.loginTimer = Timer.builder("user.login.duration")
                .description("用户登录耗时")
                .tag("type", "authentication")
                .register(meterRegistry);
        
        // 初始化注册相关指标
        this.registerAttemptCounter = Counter.builder("user.register.attempt")
                .description("用户注册尝试总数")
                .tag("type", "registration")
                .register(meterRegistry);
        
        this.registerSuccessCounter = Counter.builder("user.register.success")
                .description("用户注册成功总数")
                .tag("type", "registration")
                .register(meterRegistry);
        
        this.registerFailureCounter = Counter.builder("user.register.failure")
                .description("用户注册失败总数")
                .tag("type", "registration")
                .register(meterRegistry);
        
        this.verificationCodeSentCounter = Counter.builder("user.verification_code.sent")
                .description("验证码发送总数")
                .tag("type", "registration")
                .register(meterRegistry);
        
        // 初始化用户管理相关指标
        this.userCreatedCounter = Counter.builder("user.management.created")
                .description("管理员创建用户总数")
                .tag("type", "management")
                .register(meterRegistry);
        
        this.userUpdatedCounter = Counter.builder("user.management.updated")
                .description("用户信息更新总数")
                .tag("type", "management")
                .register(meterRegistry);
        
        this.userDeletedCounter = Counter.builder("user.management.deleted")
                .description("用户删除总数")
                .tag("type", "management")
                .register(meterRegistry);
        
        this.avatarUploadCounter = Counter.builder("user.avatar.upload")
                .description("头像上传总数")
                .tag("type", "avatar")
                .register(meterRegistry);
        
        this.avatarUploadFailureCounter = Counter.builder("user.avatar.upload.failure")
                .description("头像上传失败总数")
                .tag("type", "avatar")
                .register(meterRegistry);
        
        // 注册在线用户数量Gauge（动态指标）
        Gauge.builder("user.online.count", onlineUserCount, AtomicInteger::get)
                .description("当前在线用户数")
                .tag("type", "session")
                .register(meterRegistry);
        
        // 启动定时清理任务，移除过期的在线用户
        startSessionCleanupTask();
    }
    
    // ========== 在线用户管理方法 ==========
    
    /**
     * 用户登录成功，添加到在线用户列表
     */
    public void addOnlineUser(String userId) {
        onlineUsers.put(userId, System.currentTimeMillis());
        onlineUserCount.set(onlineUsers.size());
    }
    
    /**
     * 用户登出，从在线用户列表移除
     */
    public void removeOnlineUser(String userId) {
        onlineUsers.remove(userId);
        onlineUserCount.set(onlineUsers.size());
    }
    
    /**
     * 刷新用户会话时间（用户有活动时调用）
     */
    public void refreshUserSession(String userId) {
        if (onlineUsers.containsKey(userId)) {
            onlineUsers.put(userId, System.currentTimeMillis());
        }
    }
    
    /**
     * 启动定时任务，清理超时的在线用户
     */
    private void startSessionCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    // 每1分钟检查一次
                    Thread.sleep(60 * 1000);
                    long now = System.currentTimeMillis();
                    // 移除超时的用户
                    onlineUsers.entrySet().removeIf(entry -> 
                        now - entry.getValue() > SESSION_TIMEOUT
                    );
                    onlineUserCount.set(onlineUsers.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.setName("session-cleanup-thread");
        cleanupThread.start();
    }
}

