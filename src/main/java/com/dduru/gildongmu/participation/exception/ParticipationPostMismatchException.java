package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ParticipationPostMismatchException extends BusinessException {
    public ParticipationPostMismatchException(String message) {
        super(ErrorCode.PARTICIPATION_POST_MISMATCH,message);
    }

    public ParticipationPostMismatchException(Long participationId, Long postId) {
        super(ErrorCode.PARTICIPATION_POST_MISMATCH, String.format("해당 참여신청(id: %d)은 해당 게시글(id: %d)에 속해있지 않습니다.", participationId, postId));
    }
}
