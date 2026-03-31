package com.dduru.gildongmu.S3.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

import java.util.List;

public class InvalidFileExtensionException extends BusinessException {
    public InvalidFileExtensionException(String message) {
        super(ErrorCode.INVALID_FILE_EXTENSION, message);
    }

    public static InvalidFileExtensionException invalidExtension(List<String> allowedExtensions) {
        return new InvalidFileExtensionException("허용되지 않는 파일 확장자입니다. 허용 확장자: " + allowedExtensions);
    }
}
