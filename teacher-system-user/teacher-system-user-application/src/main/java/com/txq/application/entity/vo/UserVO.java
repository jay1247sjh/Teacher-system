package com.txq.application.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 用户信息VO
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserVO {

    // 访问令牌
    private String token;

    // 令牌过期时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Instant expireAt;

    // 用户工号
    private String wordId;

    // 用户名
    private String username;

    // 用户头像
    private String avatar;

    // 用户权限标识列表（基于RBAC，只返回权限，不返回角色）
    private List<String> permissions;

    // 动态路由树
    private RouteVO route;
}
