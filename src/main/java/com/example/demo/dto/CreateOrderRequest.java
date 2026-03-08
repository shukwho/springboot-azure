package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateOrderRequest {

  @NotNull
  private Long customerId;

  @NotEmpty
  private List<@Valid OrderLine> items;

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class OrderLine {
    @NotNull
    private Long productId;

    @NotNull @Min(1) @Max(50)
    private Integer quantity;
  }
}
