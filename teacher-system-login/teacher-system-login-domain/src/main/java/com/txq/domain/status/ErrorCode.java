package com.txq.domain.status;

/**
 * 业务错误编码
 */
public class ErrorCode {

    // 登录注册模块业务基本编码
    private final static int LOGIN_BIZ_BASE_CODE = 10000;

    // 工号校验失败
    public final static int WORK_ID_VALIDATE_ERROR_CODE = LOGIN_BIZ_BASE_CODE + 1;

    // 密码格式不符合
    public final static int PASSWORD_VALIDATE_ERROR_CODE = LOGIN_BIZ_BASE_CODE + 2;
}
