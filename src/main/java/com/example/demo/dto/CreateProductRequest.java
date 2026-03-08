package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateProductRequest {
  @NotBlank @Size(min = 3, max = 40)
  private String sku;

  @NotBlank @Size(min = 2, max = 150)
  private String name;

  @NotNull @DecimalMin("0.01")
  private BigDecimal price;

  @NotNull @Min(0)
  private Integer stock;
}
