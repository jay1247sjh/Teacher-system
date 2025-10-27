package com.txq.application.entity.vo;

import java.time.Instant;
import java.util.List;

/**
 * 用户信息VO
 */
public class UserVO {

    // 访问令牌
    private String token;

    // 令牌过期时间
    private Instant expireAt;

    // 用户工号
    private String wordId;

    // 用户名
    private String username;

    // 用户头像
    private String avatar;

    // 用户权限列表
    private List<String> permissions;

    // 动态路由树
}
