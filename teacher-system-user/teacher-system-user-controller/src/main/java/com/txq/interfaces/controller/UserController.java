package com.txq.interfaces.controller;

import com.txq.application.entity.vo.SimpleUserVO;
import com.txq.application.entity.vo.UserManagementVO;
import com.txq.application.entity.vo.UserVO;
import com.txq.application.service.IUserService;
import com.txq.common.annotation.ApiRequestMapping;
import com.txq.common.result.Response;
import com.txq.interfaces.converter.UserConverter;
import com.txq.interfaces.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器
 */
@Slf4j
@ApiRequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService loginService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Response<String> register(@RequestBody UserDTO userDTO) {
        loginService.register(
                UserConverter.INSTANCE.toQuery(userDTO)
        );
        return Response.success("注册成功");
    }

    /**
     * 获取注册验证码
     */
    @PostMapping("/send-code")
    public Response<String> sendCode(@RequestBody EmailDTO emailDTO) {
        loginService.sendCode(
                UserConverter.INSTANCE.toQuery(emailDTO)
        );
        return Response.success("获取验证码成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Response<UserVO> login(@RequestBody LoginDTO loginDTO) {
        UserVO userVO = loginService.login(
                UserConverter.INSTANCE.toQuery(loginDTO)
        );
        return Response.success(userVO);
    }
    
    /**
     * 获取所有普通用户列表
     */
    @GetMapping("/normal-users")
    public Response<List<SimpleUserVO>> getNormalUsers() {
        List<SimpleUserVO> users = loginService.getNormalUsers();
        return Response.success(users);
    }
    
    /**
     * 获取所有用户列表（管理功能）- 仅超级管理员
     */
    @GetMapping("/management/list")
    @com.txq.common.annotation.RequiresRole(1)  // 仅超级管理员
    public Response<List<UserManagementDTO>> getAllUsersForManagement() {
        log.info("获取所有用户列表（管理功能）");
        List<UserManagementVO> users = loginService.getAllUsersForManagement();
        
        // 转换为DTO
        List<UserManagementDTO> dtoList = users.stream()
                .map(vo -> UserManagementDTO.builder()
                        .id(vo.getId())
                        .username(vo.getUsername())
                        .email(vo.getEmail())
                        .roleIds(vo.getRoleIds())
                        .roleNames(vo.getRoleNames())
                        .createTime(vo.getCreateTime())
                        .updateTime(vo.getUpdateTime())
                        .build())
                .collect(Collectors.toList());
        
        return Response.success(dtoList);
    }
    
    /**
     * 创建用户 - 仅超级管理员
     */
    @PostMapping("/management/create")
    @com.txq.common.annotation.RequiresRole(1)  // 仅超级管理员
    public Response<String> createUser(@RequestBody CreateUserDTO dto) {
        log.info("创建用户，工号: {}, 用户名: {}", dto.getId(), dto.getUsername());
        loginService.createUser(dto.getId(), dto.getUsername(), dto.getPassword(), 
                dto.getEmail(), dto.getCode(), dto.getRoleIds());
        return Response.success("用户创建成功");
    }
    
    /**
     * 更新用户 - 仅超级管理员
     */
    @PostMapping("/management/update")
    @com.txq.common.annotation.RequiresRole(1)  // 仅超级管理员
    public Response<String> updateUser(@RequestBody UpdateUserDTO dto) {
        log.info("更新用户，工号: {}, 用户名: {}", dto.getId(), dto.getUsername());
        loginService.updateUser(dto.getId(), dto.getUsername(), dto.getEmail(), 
                dto.getRoleIds(), dto.getNewPassword());
        return Response.success("用户更新成功");
    }
    
    /**
     * 删除用户 - 仅超级管理员
     */
    @DeleteMapping("/management/delete/{id}")
    @com.txq.common.annotation.RequiresRole(1)  // 仅超级管理员
    public Response<String> deleteUser(@PathVariable String id) {
        log.info("删除用户，工号: {}", id);
        loginService.deleteUser(id);
        return Response.success("用户删除成功");
    }
}
