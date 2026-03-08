package com.example.demo.controller;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SalesOrderRepository;
import jakarta.validation.Valid;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final SalesOrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public OrderResponse create(@Valid @RequestBody CreateOrderRequest req) {
    Customer customer = customerRepository.findById(req.getCustomerId())
        .orElseThrow(() -> new NotFoundException("Customer not found: " + req.getCustomerId()));

    SalesOrder order = SalesOrder.builder()
        .customer(customer)
        .createdAt(OffsetDateTime.now())
        .build();

    for (CreateOrderRequest.OrderLine line : req.getItems()) {
      Product product = productRepository.findById(line.getProductId())
          .orElseThrow(() -> new NotFoundException("Product not found: " + line.getProductId()));

      // Scenario: invalid business rule -> if quantity > stock, we throw 409 by using DataIntegrityViolationException
      if (product.getStock() < line.getQuantity()) {
        throw new org.springframework.dao.DataIntegrityViolationException(
            "Insufficient stock for productId=" + product.getId() + " stock=" + product.getStock() + " qty=" + line.getQuantity());
      }

      // reduce stock (so you can test repeat orders causing conflict)
      product.setStock(product.getStock() - line.getQuantity());
      productRepository.save(product);

      OrderItem item = OrderItem.builder()
          .order(order)
          .product(product)
          .quantity(line.getQuantity())
          .build();
      order.getItems().add(item);
    }

    SalesOrder saved = orderRepository.save(order);
    log.debug("Created order id={} customerId={}", saved.getId(), customer.getId());
    return toResponse(saved);
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public OrderResponse get(@PathVariable Long id) {
    SalesOrder order = orderRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    // force initialize items before returning
    order.getItems().size();
    return toResponse(order);
  }

  @GetMapping
  @Transactional(readOnly = true)
  public List<OrderResponse> list() {
    return orderRepository.findAll().stream()
        .peek(o -> o.getItems().size())
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    if (!orderRepository.existsById(id)) {
      throw new NotFoundException("Order not found: " + id);
    }
    orderRepository.deleteById(id);
  }

  private OrderResponse toResponse(SalesOrder o) {
    List<OrderLineResponse> lines = o.getItems().stream().map(it ->
        new OrderLineResponse(it.getProduct().getId(), it.getProduct().getSku(), it.getQuantity())
    ).toList();
    return new OrderResponse(o.getId(), o.getCustomer().getId(), o.getCreatedAt().toString(), lines);
  }

  @Getter @AllArgsConstructor
  public static class OrderResponse {
    private Long orderId;
    private Long customerId;
    private String createdAt;
    private List<OrderLineResponse> items;
  }

  @Getter @AllArgsConstructor
  public static class OrderLineResponse {
    private Long productId;
    private String sku;
    private Integer quantity;
  }
}
