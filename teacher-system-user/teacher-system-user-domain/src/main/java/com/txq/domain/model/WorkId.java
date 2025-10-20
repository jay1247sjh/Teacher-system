package com.txq.domain.model;

import com.txq.common.exception.BizException;

import static com.txq.domain.status.ErrorCode.WORK_ID_VALIDATE_ERROR_CODE;

public class WorkId {
    private String id;

    public WorkId() {

    }

    public WorkId(String id) {
        this.id = id;
    }

    /**
     * 校验并返回
     * */
    public static WorkId of(String id) {
        validateIdFormat(id);
        return new WorkId(id);
    }

    // 获取工号
    public String getId() {
        return id;
    }

    /**
     * 校验工号
     */
    private static void validateIdFormat(String id) {
        if (id == null || id.length() != 10) {
            throw new BizException(WORK_ID_VALIDATE_ERROR_CODE, "工号格式不正确");
        }
    }
}
