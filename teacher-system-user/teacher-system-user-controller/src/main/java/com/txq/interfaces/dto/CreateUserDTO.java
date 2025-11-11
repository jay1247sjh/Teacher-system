package com.txq.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建用户DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    
    /**
     * 工号
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 邮箱验证码
     */
    private String code;
    
    /**
     * 角色ID列表
     */
    private List<Integer> roleIds;
}

