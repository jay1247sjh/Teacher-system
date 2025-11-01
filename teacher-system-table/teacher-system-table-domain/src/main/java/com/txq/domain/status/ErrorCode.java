package com.txq.domain.status;

/**
 * 业务错误编码
 */
public class ErrorCode {

    // 模块业务基本编码
    private final static int TABLE_BIZ_BASE_CODE = 20000;

    // 表格名称重复
    public final static int TABLE_EXIST_ERROR_CODE = TABLE_BIZ_BASE_CODE + 1;

    // 用户未登录或权限信息缺失
    public final static int USER_NOT_LOGIN_ERROR_CODE = TABLE_BIZ_BASE_CODE + 2;

    // 普通成员无权限设置分数
    public final static int NO_PERMISSION_SET_SCORE_ERROR_CODE = TABLE_BIZ_BASE_CODE + 3;

    // 数据不存在
    public final static int TABLE_DATA_NOT_EXIST_ERROR_CODE = TABLE_BIZ_BASE_CODE + 4;

    // 无权删除他人创建的数据
    public final static int NO_PERMISSION_DELETE_DATA_ERROR_CODE = TABLE_BIZ_BASE_CODE + 5;
}
