package com.bank.common.util;

import org.slf4j.MDC;

public final class TraceIdUtil {
    private static final String TRACE_ID_KEY = "traceId";

    private TraceIdUtil() {}

    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
}
