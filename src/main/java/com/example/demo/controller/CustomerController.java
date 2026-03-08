package com.example.demo.controller;

import com.example.demo.dto.CreateCustomerRequest;
import com.example.demo.dto.UpdateCustomerRequest;
import com.example.demo.entity.Customer;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerRepository customerRepository;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Customer create(@Valid @RequestBody CreateCustomerRequest req) {
    if (customerRepository.existsByEmail(req.getEmail())) {
      throw new DataIntegrityViolationException("Email already exists: " + req.getEmail());
    }
    Customer c = Customer.builder()
        .name(req.getName())
        .email(req.getEmail())
        .active(req.getActive())
        .build();
    Customer saved = customerRepository.save(c);
    log.debug("Created customer id={}", saved.getId());
    return saved;
  }

  @GetMapping
  public List<Customer> list() {
    return customerRepository.findAll();
  }

  @GetMapping("/{id}")
  public Customer get(@PathVariable Long id) {
    return customerRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
  }

  @PutMapping("/{id}")
  public Customer update(@PathVariable Long id, @Valid @RequestBody UpdateCustomerRequest req) {
    Customer existing = customerRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Customer not found: " + id));

    if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(existing.getEmail())) {
      if (customerRepository.existsByEmail(req.getEmail())) {
        throw new DataIntegrityViolationException("Email already exists: " + req.getEmail());
      }
      existing.setEmail(req.getEmail());
    }
    if (req.getName() != null) existing.setName(req.getName());
    if (req.getActive() != null) existing.setActive(req.getActive());

    return customerRepository.save(existing);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    if (!customerRepository.existsById(id)) {
      throw new NotFoundException("Customer not found: " + id);
    }
    customerRepository.deleteById(id);
  }
}
