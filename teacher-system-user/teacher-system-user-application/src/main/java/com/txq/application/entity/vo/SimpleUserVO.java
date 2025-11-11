package com.txq.application.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简单用户信息VO（用于下拉列表等场景）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUserVO {
    /**
     * 用户工号
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
}

