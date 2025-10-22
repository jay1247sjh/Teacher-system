package com.txq.application.service;

import com.txq.application.query.EmailQuery;
import com.txq.application.query.UserQuery;

/**
 * 登录注册接口
 */
public interface ILoginService {
    void register(UserQuery registerDTO);

    void sendCode(EmailQuery emailQuery);
}
