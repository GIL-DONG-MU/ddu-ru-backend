package com.dduru.gildongmu.common.exception;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OpenAPI 에러 응답 자동 추가 커스터마이저
 * 
 * @ApiErrorResponses 어노테이션에 ErrorCode enum을 지정하면,
 * 해당 ErrorCode의 HTTP 상태 코드와 메시지가 자동으로 사용됩니다.
 */
@Slf4j
@Configuration
public class ApiErrorResponseDocsCustomizer {

    /**
     * ErrorResponse 스키마를 Components에 등록
     */
    @Bean
    public GlobalOpenApiCustomizer errorResponseSchemaCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }

            // ErrorResponse 스키마 정의
            Schema<?> errorResponseSchema = new Schema<>()
                    .type("object")
                    .addProperty("status", new Schema<>().type("integer").format("int32").example(400))
                    .addProperty("data", new Schema<>()
                            .type("object")
                            .addProperty("errorCode", new Schema<>().type("string").example("INVALID_INPUT_VALUE"))
                            .addProperty("field", new Schema<>().type("string").nullable(true))
                            .addProperty("message", new Schema<>().type("string").example("잘못된 입력 값입니다.")));

            components.addSchemas("ErrorResponse", errorResponseSchema);
        };
    }

    /**
     * @ApiErrorResponses 어노테이션에 지정된 ErrorCode enum을 기반으로 에러 응답 자동 추가
     */
    @Bean
    public GlobalOperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            ErrorCode[] errorCodes = new ErrorCode[0];

            Method method = handlerMethod.getMethod();
            Class<?> declaringClass = method.getDeclaringClass();
            String methodName = method.getName();
            
            // 모든 인터페이스에서 메서드를 찾아 어노테이션 확인
            for (Class<?> iface : declaringClass.getInterfaces()) {
                // 인터페이스의 모든 메서드 검색 (파라미터 타입이 다를 수 있으므로)
                for (Method interfaceMethod : iface.getMethods()) {
                    // 메서드 이름이 같고 어노테이션이 있으면 사용
                    if (interfaceMethod.getName().equals(methodName) 
                            && interfaceMethod.isAnnotationPresent(ApiErrorResponses.class)) {
                        errorCodes = interfaceMethod.getAnnotation(ApiErrorResponses.class).value();
                        break;
                    }
                }
                if (errorCodes.length > 0) {
                    break;
                }
                
                // 인터페이스 레벨 어노테이션 확인
                if (iface.isAnnotationPresent(ApiErrorResponses.class)) {
                    errorCodes = iface.getAnnotation(ApiErrorResponses.class).value();
                    break;
                }
            }

            // 클래스 레벨 어노테이션 확인
            if (errorCodes.length == 0 && declaringClass.isAnnotationPresent(ApiErrorResponses.class)) {
                errorCodes = declaringClass.getAnnotation(ApiErrorResponses.class).value();
            }

            // 메서드 레벨 어노테이션 확인 (메서드 레벨이 우선)
            if (method.isAnnotationPresent(ApiErrorResponses.class)) {
                errorCodes = method.getAnnotation(ApiErrorResponses.class).value();
            }

            if (errorCodes.length > 0) {
                addErrorResponses(operation, errorCodes);
            }

            return operation;
        };
    }

    /**
     * ErrorCode enum 배열을 기반으로 에러 응답 추가
     * 각 ErrorCode에 대해 해당하는 HTTP 상태 코드의 응답을 추가하고, 예시를 포함합니다.
     * 같은 HTTP 상태 코드를 가진 여러 ErrorCode가 있으면 모두 examples에 추가합니다.
     */
    private void addErrorResponses(io.swagger.v3.oas.models.Operation operation, ErrorCode[] errorCodes) {
        // ErrorResponse 스키마 참조
        Schema<?> errorResponseSchema = new Schema<>()
                .$ref("#/components/schemas/ErrorResponse");

        // HTTP 상태 코드별로 ErrorCode들을 그룹화
        Map<String, java.util.List<ErrorCode>> statusCodeToErrorCodes = new HashMap<>();

        for (ErrorCode errorCode : errorCodes) {
            String statusCode = String.valueOf(errorCode.getStatus());
            statusCodeToErrorCodes.computeIfAbsent(statusCode, k -> new java.util.ArrayList<>()).add(errorCode);
        }

        // 각 상태 코드에 대해 에러 응답 추가
        for (Map.Entry<String, java.util.List<ErrorCode>> entry : statusCodeToErrorCodes.entrySet()) {
            String statusCode = entry.getKey();
            java.util.List<ErrorCode> errorCodeList = entry.getValue();
            
            // 이미 해당 상태 코드의 응답이 있으면 스킵
            if (operation.getResponses().containsKey(statusCode)) {
                continue;
            }

            // 첫 번째 ErrorCode를 기본으로 사용 (description)
            ErrorCode primaryErrorCode = errorCodeList.get(0);
            
            // 각 ErrorCode에 대한 예시 생성
            Map<String, Example> examples = new HashMap<>();
            for (ErrorCode errorCode : errorCodeList) {
                Map<String, Object> errorResponseMap = new LinkedHashMap<>();
                errorResponseMap.put("status", errorCode.getStatus());
                
                Map<String, Object> dataMap = new LinkedHashMap<>();
                dataMap.put("errorCode", errorCode.name());
                dataMap.put("message", errorCode.getMessage());
                errorResponseMap.put("data", dataMap);
                
                Example example = new Example();
                example.setValue(errorResponseMap);
                example.setSummary(errorCode.getMessage());
                examples.put(errorCode.name(), example);
            }
            
            // MediaType 생성 (여러 예시가 있으면 examples 사용, 하나만 있으면 example 사용)
            MediaType mediaType = new MediaType().schema(errorResponseSchema);
            if (examples.size() > 1) {
                mediaType.setExamples(examples);
            } else if (examples.size() == 1) {
                Example singleExample = examples.values().iterator().next();
                mediaType.setExample(singleExample.getValue());
            }

            // 에러 응답 생성
            ApiResponse apiResponse = new ApiResponse()
                    .description(primaryErrorCode.getMessage())
                    .content(new Content().addMediaType("application/json", mediaType));

            operation.getResponses().addApiResponse(statusCode, apiResponse);
        }
    }
}
