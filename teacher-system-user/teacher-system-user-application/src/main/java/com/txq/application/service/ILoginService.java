package com.txq.application.service;

import com.txq.application.entity.query.EmailQuery;
import com.txq.application.entity.query.LoginQuery;
import com.txq.application.entity.query.UserQuery;
import com.txq.application.entity.vo.UserVO;

/**
 * 登录注册接口
 */
public interface ILoginService {
    /**
     * 注册
     */
    void register(UserQuery registerDTO);

    /**
     * 发送验证码
     */
    void sendCode(EmailQuery emailQuery);

    /**
     * 登录
     */
    UserVO login(LoginQuery loginQuery);
}
