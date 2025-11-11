package com.txq.domain.event;

/**
 * 发送邮件对象
 *
 * @param email    邮箱
 * @param username 用户名
 */
public record SendMailEvent(String email, String username) {

}