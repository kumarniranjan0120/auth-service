package com.app.authservice.dto.response;

import lombok.Data;

@Data
public class ApiResponse {
    private Boolean success;
    private String message;

    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message);
    }
}
