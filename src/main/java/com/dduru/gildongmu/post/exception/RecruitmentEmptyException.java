package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class RecruitmentEmptyException extends BusinessException {

    public RecruitmentEmptyException() {
        super(ErrorCode.INVALID_RECRUIT_CAPACITY);
    }

    public RecruitmentEmptyException(Long postId) {
        super(ErrorCode.INVALID_RECRUIT_CAPACITY, "모집 인원이 0명 이하입니다. postId: " + postId);
    }
}
