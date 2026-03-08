package com.example.demo.controller;

import com.example.demo.dto.CreateProductRequest;
import com.example.demo.dto.UpdateProductRequest;
import com.example.demo.entity.Product;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductRepository productRepository;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Product create(@Valid @RequestBody CreateProductRequest req) {
    if (productRepository.existsBySku(req.getSku())) {
      throw new DataIntegrityViolationException("SKU already exists: " + req.getSku());
    }
    Product p = Product.builder()
        .sku(req.getSku())
        .name(req.getName())
        .price(req.getPrice())
        .stock(req.getStock())
        .build();
    Product saved = productRepository.save(p);
    log.debug("Created product id={}", saved.getId());
    return saved;
  }

  @GetMapping
  public List<Product> list() {
    return productRepository.findAll();
  }

  @GetMapping("/{id}")
  public Product get(@PathVariable Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Product not found: " + id));
  }

  @PutMapping("/{id}")
  public Product update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest req) {
    Product existing = productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Product not found: " + id));

    if (req.getSku() != null && !req.getSku().equalsIgnoreCase(existing.getSku())) {
      if (productRepository.existsBySku(req.getSku())) {
        throw new DataIntegrityViolationException("SKU already exists: " + req.getSku());
      }
      existing.setSku(req.getSku());
    }
    if (req.getName() != null) existing.setName(req.getName());
    if (req.getPrice() != null) existing.setPrice(req.getPrice());
    if (req.getStock() != null) existing.setStock(req.getStock());

    return productRepository.save(existing);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    if (!productRepository.existsById(id)) {
      throw new NotFoundException("Product not found: " + id);
    }
    productRepository.deleteById(id);
  }
}
