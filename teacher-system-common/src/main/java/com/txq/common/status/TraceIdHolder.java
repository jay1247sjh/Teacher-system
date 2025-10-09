package com.txq.common.status;

/**
 * 链路ID缓存器
 */
public class TraceIdHolder {
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    /**
     * 设置缓存链路ID
     */
    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    /**
     * 获取缓存链路ID
     */
    public static String getTraceId() {
        return TRACE_ID.get();
    }

    /**
     * 清除缓存链路ID
     */
    public static void clear() {
        TRACE_ID.remove();
    }
}
