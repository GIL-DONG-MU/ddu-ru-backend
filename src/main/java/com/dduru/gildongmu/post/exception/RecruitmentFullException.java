package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;


public class RecruitmentFullException extends BusinessException {
    public RecruitmentFullException() {
        super(ErrorCode.RECRUITMENT_FULL);
    }

    public RecruitmentFullException(Long postId) {
        super(ErrorCode.RECRUITMENT_FULL, "모집이 완료되었습니다. postId: " + postId);
    }
}