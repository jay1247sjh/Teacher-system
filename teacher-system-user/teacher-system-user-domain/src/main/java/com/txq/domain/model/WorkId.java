package com.txq.domain.model;

import com.txq.common.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.txq.domain.status.ErrorCode.WORK_ID_VALIDATE_ERROR_CODE;

/**
 * 工号领域对象
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class WorkId {
    // 工号字符串
    private String id;

    /**
     * 校验并返回
     */
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
