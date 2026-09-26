package com.portfolio.ems.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private Integer status;

    private String message;

    @JsonProperty("error_details")
    private String errorDetails;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("validation_errors")
    private List<ValidationError> validationErrors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationError {

        @JsonProperty("field_name")
        private String fieldName;

        @JsonProperty("error_message")
        private String errorMessage;
    }
}
