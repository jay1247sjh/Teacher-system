package com.txq.application.service;

import com.txq.application.command.UserCommand;

/**
 * 登录注册接口
 */
public interface ILoginService {
    void register(UserCommand registerDTO);
}
