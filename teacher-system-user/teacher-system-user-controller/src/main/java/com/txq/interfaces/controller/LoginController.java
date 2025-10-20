package com.txq.interfaces.controller;

import com.txq.application.service.ILoginService;
import com.txq.common.annotation.ApiRequestMapping;
import com.txq.common.result.Response;
import com.txq.interfaces.converter.UserConverter;
import com.txq.interfaces.entity.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 登录注册控制器
 */
@ApiRequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final ILoginService loginService;

    // 普通用户注册
    @PostMapping("/register")
    public Response<String> register(@RequestBody UserDTO userDTO) {
        loginService.register(
                UserConverter.INSTANCE.toCommand(userDTO)
        );
        return Response.success("注册成功");
    }
}
