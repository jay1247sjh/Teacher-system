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

    // 验证码错误
    public final static int EMAIL_CODE_VALIDATE_ERROR_CODE = USER_BIZ_BASE_CODE + 5;

    // 账号不存在
    public final static int ACCOUNT_NOT_EXIST_ERROR_CODE = USER_BIZ_BASE_CODE + 6;

    // 密码错误
    public final static int PASSWORD_ERROR_CODE = USER_BIZ_BASE_CODE + 7;
    
    // 头像相关错误码
    // 文件为空
    public final static int AVATAR_FILE_EMPTY_ERROR_CODE = USER_BIZ_BASE_CODE + 8;
    
    // 文件大小超限
    public final static int AVATAR_FILE_SIZE_ERROR_CODE = USER_BIZ_BASE_CODE + 9;
    
    // 文件格式不支持
    public final static int AVATAR_FILE_FORMAT_ERROR_CODE = USER_BIZ_BASE_CODE + 10;
    
    // 头像URL为空
    public final static int AVATAR_URL_EMPTY_ERROR_CODE = USER_BIZ_BASE_CODE + 11;
    
    // 头像URL格式错误
    public final static int AVATAR_URL_FORMAT_ERROR_CODE = USER_BIZ_BASE_CODE + 12;
    
    // 文件上传失败
    public final static int AVATAR_UPLOAD_ERROR_CODE = USER_BIZ_BASE_CODE + 13;
}
