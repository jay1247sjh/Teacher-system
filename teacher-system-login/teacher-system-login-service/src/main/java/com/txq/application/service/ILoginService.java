package com.txq.application.service;

import com.txq.application.dto.RegisterDTO;

/**
 * 登录注册接口
 */
public interface ILoginService {
    void register(RegisterDTO registerDTO);
}
