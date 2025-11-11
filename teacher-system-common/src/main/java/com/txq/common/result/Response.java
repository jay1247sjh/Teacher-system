package com.txq.common.result;

import com.txq.common.status.TraceIdHolder;
import lombok.Data;

import java.time.Instant;

/**
 * 响应结果类
 */
@Data
public class Response<T> {
    /**
     * 状态码：200表示完全成功，其他非2xx表示失败
     */
    private int code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 时间戳
     */
    private long timestamp;

    public Response() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public Response(int code, String msg, String traceId) {
        this.code = code;
        this.msg = msg;
        this.data = null;
        this.traceId = traceId;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public Response(ResultCode resultCode, T data, String traceId) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.data = data;
        this.traceId = traceId;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public static Response<String> success() {
        return new Response<>(ResultCode.SUCCESS, "请求成功", TraceIdHolder.getTraceId());
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(ResultCode.SUCCESS, data, TraceIdHolder.getTraceId());
    }

    public static <T> Response<T> fail(ResultCode resultCode) {
        return new Response<>(resultCode, null, TraceIdHolder.getTraceId());
    }

    public static <T> Response<T> fail(int code, String msg) {
        return new Response<>(code, msg, TraceIdHolder.getTraceId());
    }

    /**
     * 返回类型枚举
     */
    public enum ResultCode {
        SUCCESS(200, "成功"),
        BAD_REQUEST(400, "请求错误"),
        UNAUTHORIZED(401, "未认证"),
        FORBIDDEN(403, "无权限"),
        NOT_FOUND(404, "资源不存在"),
        SERVER_ERROR(500, "服务器内部错误"),
        BUSINESS_ERROR(1001, "业务异常");

        private final int code;

        private final String msg;

        ResultCode(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
