package ru.klimov.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseDto {

    private String message;
    private boolean result;
}
