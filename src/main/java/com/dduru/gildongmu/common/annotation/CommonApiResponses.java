package com.dduru.gildongmu.common.annotation;

import com.dduru.gildongmu.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "InvalidInput",
                                value = """
                                        {
                                          "status": 400,
                                          "data": {
                                            "errorCode": "INVALID_INPUT_VALUE",
                                            "field": null,
                                            "message": "잘못된 입력 값입니다."
                                          }
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "Unauthorized",
                                value = """
                                        {
                                          "status": 401,
                                          "data": {
                                            "errorCode": "UNAUTHORIZED",
                                            "field": null,
                                            "message": "인증되지 않은 사용자입니다."
                                          }
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "리소스를 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "NotFound",
                                value = """
                                        {
                                          "status": 404,
                                          "data": {
                                            "errorCode": "NOT_FOUND",
                                            "field": null,
                                            "message": "요청한 리소스를 찾을 수 없습니다."
                                          }
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "InternalServerError",
                                value = """
                                        {
                                          "status": 500,
                                          "data": {
                                            "errorCode": "INTERNAL_SERVER_ERROR",
                                            "field": null,
                                            "message": "서버 오류가 발생했습니다."
                                          }
                                        }
                                        """
                        )
                )
        )
})
public @interface CommonApiResponses {
}
