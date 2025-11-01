package com.txq.domain.status;

/**
 * 业务错误编码
 */
public class ErrorCode {

    // 模块业务基本编码
    private final static int TABLE_BIZ_BASE_CODE = 20000;

    // 表格名称重复
    public final static int TABLE_EXIST_ERROR_CODE = TABLE_BIZ_BASE_CODE + 1;
}
