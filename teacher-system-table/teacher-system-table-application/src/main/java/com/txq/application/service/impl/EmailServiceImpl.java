package com.txq.application.service.impl;

import com.txq.application.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 邮件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Override
    public void sendRejectNotification(String toEmail, String userName, String userId, String tableName, 
                                      String submissionPeriod, Map<String, Object> dataContent, 
                                      String rejectReason, Long dataId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【数据退回通知】" + tableName);
            
            // 格式化提交时期
            String periodText = submissionPeriod != null ? submissionPeriod : "未设置";
            
            // 构建数据内容表格
            StringBuilder dataTableRows = new StringBuilder();
            if (dataContent != null && !dataContent.isEmpty()) {
                for (Map.Entry<String, Object> entry : dataContent.entrySet()) {
                    String value = entry.getValue() != null ? entry.getValue().toString() : "-";
                    dataTableRows.append(String.format(
                        "<tr>" +
                        "  <td style=\"padding: 7px 8px; border-bottom: 1px solid #e0e0e0; color: #666; width: 110px; white-space: nowrap;\">%s</td>" +
                        "  <td style=\"padding: 7px 8px; border-bottom: 1px solid #e0e0e0; color: #333; font-weight: 500; word-break: break-all;\">%s</td>" +
                        "</tr>",
                        entry.getKey(), value
                    ));
                }
            } else {
                dataTableRows.append(
                    "<tr>" +
                    "  <td colspan=\"2\" style=\"padding: 8px; text-align: center; color: #999;\">无数据内容</td>" +
                    "</tr>"
                );
            }
            
            // 获取当前时间
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
            
            // HTML 邮件内容
            String content = String.format(
                "<div style=\"font-family: 'Microsoft YaHei', Arial, sans-serif; max-width: 650px; margin: 0 auto; padding: 20px; " +
                "border: 1px solid #e0e0e0; border-radius: 10px; background-color: #fafafa;\">" +
                
                "  <div style=\"text-align: center; padding-bottom: 15px; border-bottom: 2px solid #ff9800;\">" +
                "    <h2 style=\"color: #ff9800; margin: 0; font-size: 22px;\">📋 数据退回通知</h2>" +
                "    <p style=\"color: #999; font-size: 11px; margin: 5px 0 0 0;\">%s</p>" +
                "  </div>" +
                
                "  <div style=\"padding: 15px 0;\">" +
                "    <p style=\"margin: 0; font-size: 15px;\">尊敬的 <b style=\"color: #2e6da4;\">%s</b>（工号：%s）：</p>" +
                "    <p style=\"margin: 8px 0 0 0; color: #666; font-size: 14px;\">您提交的数据已被退回，请查看详情并修改。</p>" +
                "  </div>" +
                
                "  <div style=\"background: white; border-radius: 6px; padding: 15px; margin: 15px 0; border: 1px solid #e0e0e0;\">" +
                "    <h3 style=\"color: #333; margin: 0 0 12px 0; font-size: 15px; border-bottom: 2px solid #2e6da4; padding-bottom: 6px;\">📊 数据信息</h3>" +
                "    <table style=\"width: 100%%; border-collapse: collapse; font-size: 14px;\">" +
                "      <tr>" +
                "        <td style=\"padding: 8px; border-bottom: 1px solid #e0e0e0; color: #666; width: 90px; white-space: nowrap;\">数据编号</td>" +
                "        <td style=\"padding: 8px; border-bottom: 1px solid #e0e0e0; color: #333; font-weight: 500;\">#%d</td>" +
                "      </tr>" +
                "      <tr>" +
                "        <td style=\"padding: 8px; border-bottom: 1px solid #e0e0e0; color: #666; white-space: nowrap;\">所属表格</td>" +
                "        <td style=\"padding: 8px; border-bottom: 1px solid #e0e0e0; color: #333; font-weight: 500;\">%s</td>" +
                "      </tr>" +
                "      <tr>" +
                "        <td style=\"padding: 8px; border-bottom: 1px solid #e0e0e0; color: #666; white-space: nowrap;\">成果时期</td>" +
                "        <td style=\"padding: 8px; border-bottom: 1px solid #e0e0e0; color: #333; font-weight: 500;\">%s</td>" +
                "      </tr>" +
                "    </table>" +
                "  </div>" +
                
                "  <div style=\"background: white; border-radius: 6px; padding: 15px; margin: 15px 0; border: 1px solid #e0e0e0;\">" +
                "    <h3 style=\"color: #333; margin: 0 0 12px 0; font-size: 15px; border-bottom: 2px solid #2e6da4; padding-bottom: 6px;\">📝 数据内容</h3>" +
                "    <table style=\"width: 100%%; border-collapse: collapse; font-size: 13px;\">" +
                "      %s" +
                "    </table>" +
                "  </div>" +
                
                "  <div style=\"background: #fff3e0; border-left: 4px solid #ff9800; padding: 15px; margin: 15px 0; border-radius: 4px;\">" +
                "    <h3 style=\"color: #f57c00; margin: 0 0 8px 0; font-size: 15px;\">⚠️ 退回原因</h3>" +
                "    <p style=\"white-space: pre-wrap; color: #333; margin: 0; line-height: 1.5; font-size: 14px;\">%s</p>" +
                "  </div>" +
                
                "  <div style=\"background: #e3f2fd; padding: 12px; border-radius: 4px; margin: 15px 0; border-left: 4px solid #2196f3;\">" +
                "    <p style=\"margin: 0; color: #1976d2; font-size: 13px;\">💡 <b>提示：</b>请修改数据后重新提交</p>" +
                "  </div>" +
                
                "  <hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">" +
                "  <div style=\"text-align: center;\">" +
                "    <p style=\"font-size: 11px; color: #999; margin: 3px 0;\">本邮件由系统自动发送，请勿回复</p>" +
                "    <p style=\"font-size: 11px; color: #999; margin: 3px 0;\">外国语学院科研系统</p>" +
                "  </div>" +
                "</div>",
                currentTime, userName, userId, dataId, tableName, periodText, dataTableRows.toString(), rejectReason
            );
            
            helper.setText(content, true);  // true 表示使用 HTML 格式
            
            mailSender.send(message);
            log.info("退回通知邮件发送成功: toEmail={}, tableName={}, userName={}, dataId={}", 
                    toEmail, tableName, userName, dataId);
        } catch (MessagingException e) {
            log.error("发送退回通知邮件失败: toEmail={}, tableName={}, 错误信息: {}", 
                    toEmail, tableName, e.getMessage(), e);
            // 邮件发送失败不影响业务主流程，只记录日志
        } catch (Exception e) {
            log.error("发送退回通知邮件时发生未知错误: toEmail={}, tableName={}", toEmail, tableName, e);
        }
    }
}

