package com.example.demo.exception;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiError {
  private OffsetDateTime timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private String traceId;
  private List<FieldViolation> violations;

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class FieldViolation {
    private String field;
    private String message;
  }
}
