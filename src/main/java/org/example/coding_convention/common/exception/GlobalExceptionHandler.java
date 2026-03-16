package org.example.coding_convention.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.coding_convention.common.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static org.example.coding_convention.common.response.BaseResponseStatus.REQUEST_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private int httpStatusCodeMapper(int statusCode) {
        if(statusCode >= 40000) {
            return 500;
        } else {
            return 400;
        }

    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for(FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.error(errors.toString());
        return ResponseEntity.status(400).body(BaseResponse.error(REQUEST_ERROR, errors));
    }


    @ExceptionHandler(BaseException.class)
    public ResponseEntity handleException(BaseException e) {
        // BaseResponse 없이
//        return ResponseEntity.status(500).body("예외처리 : " + e.getMessage());
        // BaseResponse 사용
        return ResponseEntity
                .status(httpStatusCodeMapper(e.getStatus().getCode()))
                .body(BaseResponse.error(e.getStatus()));
    }

}
