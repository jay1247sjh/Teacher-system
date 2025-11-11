package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理VO（超级管理员视角）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementVO {
    
    /**
     * 工号
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
     * 角色名称列表
     */
    private List<String> roleNames;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

