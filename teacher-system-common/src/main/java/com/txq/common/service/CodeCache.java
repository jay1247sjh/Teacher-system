package com.txq.common.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 验证码缓存工具
 */
public class CodeCache {

    // 存储验证码的内容缓存
    private final Map<String, CodeEntry> codeCache = new ConcurrentHashMap<>();

    // 清理任务线程池
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        // 根据任务创建线程
        Thread thread = new Thread(r);
        // 设置为守护进程
        thread.setDaemon(true);
        // 设置名称
        thread.setName("verify-code-cleaner");
        return thread;
    });

    public CodeCache() {
        // 每一分钟执行一次清理任务
        cleaner.scheduleAtFixedRate(this::removeExpiredCodes, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 保存验证码
     */
    public void saveCode(String email, String code, long minutes) {
        // 设置过期时间
        long expireAt = Instant.now().plusSeconds(minutes * 60).toEpochMilli();
        // 保存到缓存
        codeCache.put(email, new CodeEntry(code, expireAt));
    }

    /**
     * 校验验证码是否正确 & 未过期
     */
    public boolean validateCode(String email, String inputCode) {
        // 从缓存获取数据
        CodeEntry entry = codeCache.get(email);
        // 判断是否为空或者过期
        if (entry == null || entry.isExpired()) {
            codeCache.remove(email);
            return false;
        }
        // 判断与缓存中的是否相同
        return entry.code.equals(inputCode);
    }

    /**
     * 删除验证码
     */
    public void removeCode(String email) {
        codeCache.remove(email);
    }

    /**
     * 清理过期验证码
     */
    private void removeExpiredCodes() {
        // 获取当前时间
        long now = Instant.now().toEpochMilli();
        // 如果过期就删除
        codeCache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /**
     * 验证码类
     */
    @Data
    @RequiredArgsConstructor
    private static class CodeEntry {

        // 验证码
        final String code;

        // 过期时间
        final long expireAt;

        /**
         * 判断是否过期
         */
        boolean isExpired() {
            return Instant.now().toEpochMilli() > expireAt;
        }
    }
}
