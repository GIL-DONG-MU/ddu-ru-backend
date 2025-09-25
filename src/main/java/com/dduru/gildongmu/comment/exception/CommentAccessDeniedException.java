package com.dduru.gildongmu.comment.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class CommentAccessDeniedException extends BusinessException {
    public CommentAccessDeniedException() {
        super(ErrorCode.COMMENT_ACCESS_DENIED, "댓글에 대한 접근 권한이 없습니다");
    }

    public CommentAccessDeniedException(String message) {
        super(ErrorCode.COMMENT_ACCESS_DENIED, message);
    }

    public static CommentAccessDeniedException ownerOnly() {
        return new CommentAccessDeniedException("댓글 작성자만 접근할 수 있습니다");
    }
}


