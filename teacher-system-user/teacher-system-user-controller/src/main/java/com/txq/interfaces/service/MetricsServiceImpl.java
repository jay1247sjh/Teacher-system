package com.txq.interfaces.service;

import com.txq.application.service.IMetricsService;
import com.txq.interfaces.config.MetricsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * 指标服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements IMetricsService {

    private final MetricsConfig metricsConfig;

    @Override
    public void recordLoginAttempt() {
        metricsConfig.getLoginAttemptCounter().increment();
    }

    @Override
    public void recordLoginSuccess(String userId) {
        metricsConfig.getLoginSuccessCounter().increment();
        metricsConfig.addOnlineUser(userId);
    }

    @Override
    public void recordLoginFailure() {
        metricsConfig.getLoginFailureCounter().increment();
    }

    @Override
    public <T> T recordLoginDuration(Supplier<T> loginAction) {
        return metricsConfig.getLoginTimer().record(loginAction);
    }

    @Override
    public void recordRegisterAttempt() {
        metricsConfig.getRegisterAttemptCounter().increment();
    }

    @Override
    public void recordRegisterSuccess() {
        metricsConfig.getRegisterSuccessCounter().increment();
    }

    @Override
    public void recordRegisterFailure() {
        metricsConfig.getRegisterFailureCounter().increment();
    }

    @Override
    public void recordVerificationCodeSent() {
        metricsConfig.getVerificationCodeSentCounter().increment();
    }

    @Override
    public void recordUserCreated() {
        metricsConfig.getUserCreatedCounter().increment();
    }

    @Override
    public void recordUserUpdated() {
        metricsConfig.getUserUpdatedCounter().increment();
    }

    @Override
    public void recordUserDeleted() {
        metricsConfig.getUserDeletedCounter().increment();
    }

    @Override
    public void recordAvatarUpload() {
        metricsConfig.getAvatarUploadCounter().increment();
    }

    @Override
    public void recordAvatarUploadFailure() {
        metricsConfig.getAvatarUploadFailureCounter().increment();
    }

    @Override
    public void addOnlineUser(String userId) {
        metricsConfig.addOnlineUser(userId);
    }

    @Override
    public void removeOnlineUser(String userId) {
        metricsConfig.removeOnlineUser(userId);
    }

    @Override
    public void refreshUserSession(String userId) {
        metricsConfig.refreshUserSession(userId);
    }
}

