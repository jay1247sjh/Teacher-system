package com.txq.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    // 工号
    private String id;

    // 密码
    private String password;
}
