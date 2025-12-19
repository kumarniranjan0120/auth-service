package com.app.authservice.dto.request;

import lombok.Data;

@Data
public class OAuth2CallbackRequest {
    private String code;
    private String state;
    private String error;
    private String errorDescription;
}