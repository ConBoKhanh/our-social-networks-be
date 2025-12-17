package com.oursocialnetworks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chuẩn cho tất cả API")
public class ApiResponse<T> {

    @Schema(description = "Trạng thái", example = "success")
    private String status;

    @Schema(description = "Thông báo", example = "Thành công!")
    private String message;

    @Schema(description = "HTTP status code", example = "200")
    private Integer code;

    @Schema(description = "Dữ liệu trả về")
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, 200, data);
    }

    public static <T> ApiResponse<T> error(String message, int code) {
        return new ApiResponse<>("error", message, code, null);
    }
}
