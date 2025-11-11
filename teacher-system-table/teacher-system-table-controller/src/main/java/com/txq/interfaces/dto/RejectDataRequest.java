package com.txq.interfaces.dto;

import lombok.Data;

/**
 * 退回数据请求
 */
@Data
public class RejectDataRequest {
    private Long id;              // 数据ID
    private String rejectReason;  // 退回原因
}

