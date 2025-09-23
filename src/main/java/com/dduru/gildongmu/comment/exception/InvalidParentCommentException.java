package com.dduru.gildongmu.comment.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidParentCommentException extends BusinessException {
    public InvalidParentCommentException() {
        super(ErrorCode.INVALID_PARENT_COMMENT);
    }

    public InvalidParentCommentException(String message) {
        super(ErrorCode.INVALID_PARENT_COMMENT, message);
    }

    public static InvalidParentCommentException of(Long postId, Long parentPostId) {
        return new InvalidParentCommentException(
                String.format("상위 댓글의 게시물 ID(%d)가 현재 게시물 ID(%d)와 일치하지 않습니다.", parentPostId, postId)
        );
    }
}
