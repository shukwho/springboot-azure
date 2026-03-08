package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateCustomerRequest {
  @Size(min = 2, max = 120)
  private String name;

  @Email @Size(max = 180)
  private String email;

  private Boolean active;
}
