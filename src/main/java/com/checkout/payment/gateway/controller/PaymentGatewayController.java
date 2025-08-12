package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.checkout.payment.gateway.enums.PaymentStatus.*;

@RestController("api")
public class PaymentGatewayController {
  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayController.class);
  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @GetMapping("/payment/{id}")
  public ResponseEntity<PostPaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  @PostMapping("/payment")
  public ResponseEntity<PostPaymentResponse> processPayment(@Valid @RequestBody final PostPaymentRequest postPaymentRequest) {
    LOG.info("Attempting payment {}", postPaymentRequest);

    UUID id = paymentGatewayService.processPayment(postPaymentRequest);

    PostPaymentResponse response = new PostPaymentResponse();
    response.setAmount(postPaymentRequest.getAmount());
    response.setCurrency(postPaymentRequest.getCurrency());
    response.setStatus(id == null ? DECLINED : AUTHORIZED);
    response.setId(id);
    response.setExpiryMonth(postPaymentRequest.getExpiryMonth());
    response.setExpiryYear(postPaymentRequest.getExpiryYear());
    String lastFour = postPaymentRequest.getCardNumberLastFour()
        .substring(postPaymentRequest.getCardNumberLastFour().length()-4);
    response.setCardNumberLastFour(lastFour);

    LOG.info("Processed payment {}", response);

    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
