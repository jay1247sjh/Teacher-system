package com.txq.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱验证码DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO {
    // 姓名
    private String username;

    // 邮箱
    private String email;
}
