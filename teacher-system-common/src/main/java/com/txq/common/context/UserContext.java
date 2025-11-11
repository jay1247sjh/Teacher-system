package com.txq.common.context;

import java.util.List;

/**
 * 用户上下文
 * 存储当前请求的用户信息
 */
public class UserContext {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<List<Integer>> ROLE_IDS = new ThreadLocal<>();

    /**
     * 设置用户ID
     */
    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取用户ID
     */
    public static String getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置角色ID列表
     */
    public static void setRoleIds(List<Integer> roleIds) {
        ROLE_IDS.set(roleIds);
    }

    /**
     * 获取角色ID列表
     */
    public static List<Integer> getRoleIds() {
        return ROLE_IDS.get();
    }

    /**
     * 判断是否有指定角色
     */
    public static boolean hasRole(Integer roleId) {
        List<Integer> roleIds = ROLE_IDS.get();
        return roleIds != null && roleIds.contains(roleId);
    }

    /**
     * 判断是否是超级管理员
     */
    public static boolean isSuperAdmin() {
        return hasRole(1);
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        USER_ID.remove();
        ROLE_IDS.remove();
    }
}

