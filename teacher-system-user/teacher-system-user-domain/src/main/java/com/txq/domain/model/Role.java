package com.txq.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * 角色领域模型
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Role {
    // id
    private int id;

    // 角色名称
    private String roleName;

    // 角色描述
    private String RoleDescription;

    // 权限列表
    private Set<Permission> permissions = new HashSet<>();
}
