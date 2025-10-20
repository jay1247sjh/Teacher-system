package com.txq.domain.status;

/**
 * 业务错误编码
 */
public class ErrorCode {

    // 模块业务基本编码
    private final static int USER_BIZ_BASE_CODE = 10000;

    // 账号已存在
    public final static int ACCOUNT_EXIST_ERROR_CODE = USER_BIZ_BASE_CODE + 1;

    // 工号校验失败
    public final static int WORK_ID_VALIDATE_ERROR_CODE = USER_BIZ_BASE_CODE + 2;

    // 密码格式不符合
    public final static int PASSWORD_VALIDATE_ERROR_CODE = USER_BIZ_BASE_CODE + 3;

    // 邮箱格式不符合
    public final static int EMAIL_VALIDATE_ERROR_CODE = USER_BIZ_BASE_CODE + 4;
}
