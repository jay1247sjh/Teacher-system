package com.txq.application.service;

import java.util.Map;

/**
 * 邮件服务接口
 */
public interface IEmailService {
    /**
     * 发送数据退回通知邮件
     *
     * @param toEmail      收件人邮箱
     * @param userName     用户名
     * @param userId       用户工号
     * @param tableName    表格名称
     * @param submissionPeriod 提交时期
     * @param dataContent  数据内容
     * @param rejectReason 退回原因
     * @param dataId       数据ID
     */
    void sendRejectNotification(String toEmail, String userName, String userId, String tableName, 
                                String submissionPeriod, Map<String, Object> dataContent, 
                                String rejectReason, Long dataId);
}

