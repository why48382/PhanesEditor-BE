package org.example.coding_convention.common.exception;

import lombok.Getter;
import org.example.coding_convention.common.response.BaseResponseStatus;

@Getter
public class BaseException extends RuntimeException{
    private BaseResponseStatus status;

    public BaseException(BaseResponseStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static BaseException from(BaseResponseStatus status) {
        return new BaseException(status, status.getMessage());
    }
}
