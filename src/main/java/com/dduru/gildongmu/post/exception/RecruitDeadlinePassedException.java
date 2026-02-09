package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class RecruitDeadlinePassedException extends BusinessException {
    public RecruitDeadlinePassedException() {
        super(ErrorCode.RECRUIT_DEADLINE_PASSED, "모집 마감된 게시글은 수정할 수 없습니다.");
    }

    public RecruitDeadlinePassedException(String message) {
        super(ErrorCode.RECRUIT_DEADLINE_PASSED, message);
    }
}
