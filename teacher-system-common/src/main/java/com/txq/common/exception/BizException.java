package com.txq.common.exception;

import com.txq.common.result.Response;

/**
 * 业务异常类
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(Response.ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * 获取错误编码
     */
    public int getCode() {
        return code;
    }
}
