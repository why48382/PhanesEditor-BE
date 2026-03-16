package org.example.coding_convention.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.example.coding_convention.common.response.BaseResponseStatus.SUCCESS;

@Getter
@AllArgsConstructor
@Schema(description = "API 기본 응답 형식")
public class BaseResponse<T> {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    @Schema(description = "응답 메시지", example = "요청에 성공했습니다.")
    private String message;
    @Schema(description = "실제 데이터 (제네릭)", implementation = Object.class)
    private T results;

    public static <T> BaseResponse<T> success(T results) {
        return new BaseResponse<>(SUCCESS.isSuccess(), SUCCESS.getMessage(), results);
    }

    public static <T> BaseResponse<T> error(BaseResponseStatus status) {
        return new BaseResponse<>(status.isSuccess(), status.getMessage(), null);
    }

    public static <T> BaseResponse<T> error(BaseResponseStatus status, T results) {
        return new BaseResponse<>(status.isSuccess(), status.getMessage(), results);
    }
}

