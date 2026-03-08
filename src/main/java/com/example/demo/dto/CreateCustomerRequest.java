package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateCustomerRequest {
  @NotBlank @Size(min = 2, max = 120)
  private String name;

  @NotBlank @Email @Size(max = 180)
  private String email;

  @NotNull
  private Boolean active;
}
