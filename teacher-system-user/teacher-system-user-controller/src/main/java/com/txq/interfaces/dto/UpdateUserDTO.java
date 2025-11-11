package com.txq.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新用户DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {
    
    /**
     * 工号（不可修改）
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 角色ID列表
     */
    private List<Integer> roleIds;
    
    /**
     * 新密码（可选，如果提供则修改密码）
     */
    private String newPassword;
}

