package com.oursocialnetworks.dto;

public class ApiResponse<T> {
    private String status;      // "success" | "error"
    private String message;     // Thông báo
    private T data;            // Dữ liệu trả về
    private Integer code;      // HTTP status code
    private Long timestamp;    // Timestamp

    public ApiResponse() {}

    public ApiResponse(String status, String message, T data, Integer code, Long timestamp) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.code = code;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    // Static factory methods
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data, 200, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "Thành công", data, 200, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> error(String message, Integer code) {
        return new ApiResponse<>("error", message, null, code, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null, 500, System.currentTimeMillis());
    }
}