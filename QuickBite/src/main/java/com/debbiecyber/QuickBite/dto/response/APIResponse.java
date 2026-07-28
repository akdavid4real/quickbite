package com.debbiecyber.QuickBite.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class APIResponse<T> {
    private boolean success;

    private String message;

    private T data;

    public static <T> APIResponse<T> success(String message, T data) {
        return APIResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> APIResponse<T> success(String message) {
        return APIResponse.<T>builder()
                .success(true)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> APIResponse<T> error(String message) {
        return APIResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
