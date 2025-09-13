package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class PostAccessDeniedException extends BusinessException {
    public PostAccessDeniedException() {
        super(ErrorCode.POST_ACCESS_DENIED, "게시글에 대한 접근 권한이 없습니다");
    }

    public PostAccessDeniedException(String message) {
        super(ErrorCode.POST_ACCESS_DENIED, message);
    }

    public static PostAccessDeniedException ownerOnly() {
        return new PostAccessDeniedException("게시글 작성자만 접근할 수 있습니다");
    }

    public static PostAccessDeniedException applicantOnly() {
        return new PostAccessDeniedException("신청자 본인만 접근할 수 있습니다");
    }
}
