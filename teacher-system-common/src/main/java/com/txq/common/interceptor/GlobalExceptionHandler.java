package com.txq.common.interceptor;

import com.txq.common.exception.BizException;
import com.txq.common.result.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常拦截器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public Response<?> handleBizException(BizException e) {
        log.warn("业务异常:{}", e.getMessage());
        return Response.fail(e.getCode(), e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Response<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Response.fail(Response.ResultCode.SERVER_ERROR);
    }
}
