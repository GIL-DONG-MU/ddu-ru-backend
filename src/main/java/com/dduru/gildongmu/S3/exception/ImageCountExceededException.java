package com.dduru.gildongmu.S3.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ImageCountExceededException extends BusinessException {
    public ImageCountExceededException() {
        super(ErrorCode.IMAGE_COUNT_EXCEEDED);
    }

    public ImageCountExceededException(String message) {
        super(ErrorCode.IMAGE_COUNT_EXCEEDED, message);
    }

    public static ImageCountExceededException withMax(int max) {
        return new ImageCountExceededException("이미지는 최대 " + max + "장까지 업로드할 수 있습니다.");
    }
}
