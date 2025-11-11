package com.txq.infrastructure.service.notice;

import com.txq.common.exception.BizException;
import com.txq.common.service.CodeCache;
import com.txq.domain.event.SendMailEvent;
import com.txq.domain.infra.notice.MailService;
import com.txq.infrastructure.properties.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.txq.domain.status.ErrorCode.EMAIL_CODE_VALIDATE_ERROR_CODE;

@Component
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    // 邮箱处理类
    private final JavaMailSender mailSender;

    // 邮箱信息类
    private final MailProperties mailProperties;

    // 验证码缓存类
    private final CodeCache codeCache = new CodeCache();

    /**
     * 发送邮件
     *
     * @param sendMailEvent 事件对象
     */
    @Async("mailTaskExecutor")      // 使用异步线程池
    @EventListener
    @Override
    public void sendRegisterMail(SendMailEvent sendMailEvent) {
        try {
            // 生成6位随机验证码
            long code = (int) (Math.random() * 900000) + 100000;
            // 发送者
            String sender = mailProperties.getFrom();
            // 保存到缓存，5分钟结束
            codeCache.saveCode(sendMailEvent.email(), String.valueOf(code), 5);

            MimeMessage message = mailSender.createMimeMessage();
            // 允许内嵌资源和附件，并指定为UTF-8
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            // 设置发送人
            helper.setFrom(sender);
            // 设置收件人
            helper.setTo(sendMailEvent.email());
            // 设置标题
            helper.setSubject(mailProperties.getSubject().getTitle());
            // 获取邮箱内容
            String content = mailProperties.getTemplate().getDescription().formatted(sendMailEvent.username(), code);
            // 设置邮箱内容
            helper.setText(content, true);
            // 发送邮箱
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("发送邮件失败", e);
        }
    }

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱
     * @param code  验证码
     */
    @Override
    public void validateMailCode(String email, String code) {
        if (!codeCache.validateCode(email, code)) {
            throw new BizException(EMAIL_CODE_VALIDATE_ERROR_CODE, "验证码错误");
        }
    }
}
