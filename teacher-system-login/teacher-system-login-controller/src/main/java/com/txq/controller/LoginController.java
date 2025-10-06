package com.txq.controller;

import com.txq.annotation.ApiRequestMapping;
import com.txq.response.Response;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 登录注册控制器
 */
@ApiRequestMapping("/login")
public class LoginController {

    // 普通用户注册
    @PostMapping("/register")
    public Response register() {
        return new Response();
    }
}
