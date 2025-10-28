package com.txq.application.entity.vo;

import java.util.List;

/**
 * 路由视图
 */
public class RouteVO {
    // 路由路径
    private String path;

    // 前端组件路径
    private String component;

    // 重定向路径
    private String redirect;

    // 子路由
    private List<RouteVO> children;
}
