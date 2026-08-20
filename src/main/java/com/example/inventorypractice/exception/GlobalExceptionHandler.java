package com.example.inventorypractice.exception;

import com.example.inventorypractice.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException exception) {

        // 业务异常的 code 和 message 是后端主动定义的，可以安全地返回给前端。
        return ResponseEntity.status(exception.getCode())
                .body(ApiResponse.error(
                        exception.getCode(),
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception) {

        // 当前接口一次返回第一个字段错误，避免前端收到 Spring 默认的复杂异常结构。
        FieldError fieldError =
                exception.getBindingResult().getFieldError();

        String message = fieldError == null
                ? "请求参数错误"
                : fieldError.getDefaultMessage();

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception exception) {
        // 服务端日志保留完整异常栈，便于开发人员定位真实根因。
        log.error("未处理的服务器异常", exception);

        // 前端只接收通用提示，不能暴露 SQL、路径、密码等内部异常细节。
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(500, "服务器内部错误"));
    }
}
