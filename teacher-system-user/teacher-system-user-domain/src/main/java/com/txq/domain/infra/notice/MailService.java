package com.txq.domain.infra.notice;

import com.txq.domain.event.SendMailEvent;

/**
 * 邮箱发送接口
 */
public interface MailService {
    void sendRegisterMail(SendMailEvent sendMailEvent);

    void validateMailCode(String email, String code);
}