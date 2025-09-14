package com.dduru.gildongmu.S3.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class EmptyFileException extends BusinessException {
    public EmptyFileException() {
        super(ErrorCode.EMPTY_FILE);
    }
}
