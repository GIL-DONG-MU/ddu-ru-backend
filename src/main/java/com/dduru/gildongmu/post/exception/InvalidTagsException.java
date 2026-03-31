package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidTagsException extends BusinessException {
    public InvalidTagsException(String message) {
        super(ErrorCode.INVALID_POST_TAGS, message);
    }

    public static InvalidTagsException tooMany() {
        return new InvalidTagsException("태그는 최대 4개까지 입력할 수 있습니다");
    }

    public static InvalidTagsException blankOrWhitespace() {
        return new InvalidTagsException("빈 태그이거나 공백이 포함된 태그는 사용할 수 없습니다");
    }

    public static InvalidTagsException invalidLengthOrCharacters() {
        return new InvalidTagsException("태그는 한글 음절 기준 최대 4자, 영문·숫자만 사용할 때는 최대 7자이며, 한글·영문·숫자만 사용할 수 있습니다");
    }
}
