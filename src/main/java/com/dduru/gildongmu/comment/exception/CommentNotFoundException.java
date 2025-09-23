package com.dduru.gildongmu.comment.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class CommentNotFoundException extends BusinessException {
    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND, "댓글을 찾을 수 없습니다");
    }

    public CommentNotFoundException(String message) {
        super(ErrorCode.COMMENT_NOT_FOUND, message);
    }

    public static CommentNotFoundException of(Long commentId) {
        return new CommentNotFoundException("댓글을 찾을 수 없습니다. commentId=" + commentId);
    }
}
