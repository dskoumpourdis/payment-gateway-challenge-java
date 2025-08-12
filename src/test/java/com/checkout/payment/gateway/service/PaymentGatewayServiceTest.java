package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.model.PostAuthResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import java.util.UUID;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  @InjectMocks
  private PaymentGatewayService paymentGatewayService;

  @Mock
  private PaymentsRepository paymentsRepository;

  @Mock
  private RestTemplate restTemplate;

  @Test
  void whenCardNumberEndInOddPaymentIsAuthorized(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("4321");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2026);
    payment.setCurrency("USD");
    payment.setAmount(10);
    payment.setCvv("999");


    PostAuthResponse response = new PostAuthResponse();
    response.setAuthorization_code(UUID.randomUUID().toString());

    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), any())).thenReturn(
        new ResponseEntity<>(response, HttpStatus.OK));

    UUID uuid = paymentGatewayService.processPayment(payment);

    assertFalse(uuid.toString().isEmpty());
  }

  @Test
  void whenCardIsExpiredPaymentIsNotAuthorized(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("4321");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(1990);
    payment.setCurrency("USD");
    payment.setAmount(10);
    payment.setCvv("999");

    UUID uuid = paymentGatewayService.processPayment(payment);

    assertNull(uuid);
  }

  @Test
  void whenFieldsAreMissingPaymentIsNotAuthorized(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2026);
    payment.setCurrency("");
    payment.setAmount(10);
    payment.setCvv("999");

    UUID uuid = paymentGatewayService.processPayment(payment);

    assertNull(uuid);
  }

  @Test
  void whenCardNumberIsNotNumericPaymentIsNotAuthorized(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("43fgdhbdf22");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2026);
    payment.setCurrency("USD");
    payment.setAmount(10);
    payment.setCvv("999");

    UUID uuid = paymentGatewayService.processPayment(payment);

    assertNull(uuid);
  }

  @Test
  void whenMonthIsGreaterThan12PaymentIsNotAuthorized(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("4322");
    payment.setExpiryMonth(15);
    payment.setExpiryYear(2026);
    payment.setCurrency("USD");
    payment.setAmount(10);
    payment.setCvv("999");

    UUID uuid = paymentGatewayService.processPayment(payment);

    assertNull(uuid);
  }

  @Test
  void whenCardNumberEndInEvenPaymentIsNotAuthorized(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("4322");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2026);
    payment.setCurrency("USD");
    payment.setAmount(10);
    payment.setCvv("999");

    UUID uuid = paymentGatewayService.processPayment(payment);

    assertNull(uuid);
  }

  @Test
  void whenCurrencyIsMoreThan3CharsPaymentIsNotAuthorized(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("");
    payment.setCardNumberLastFour("4322");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2026);
    payment.setCurrency("USDD");
    payment.setAmount(10);
    payment.setCvv("999");

    UUID uuid = paymentGatewayService.processPayment(payment);

    assertNull(uuid);
  }

  @Test
  void whenCVVIsMoreThan4CharsPaymentIsNotAuthorized(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("4322");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2026);
    payment.setCurrency("USD");
    payment.setAmount(10);
    payment.setCvv("99913");

    UUID uuid = paymentGatewayService.processPayment(payment);

    assertNull(uuid);
  }

  @Test
  void whenCardNumberEndIn0ServiceIsUnavailable(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("4320");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2026);
    payment.setCurrency("USD");
    payment.setAmount(10);
    payment.setCvv("9993");

    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), any())).thenThrow(new HttpServerErrorException(
        HttpStatus.SERVICE_UNAVAILABLE));

    assertThrows(
        HttpServerErrorException.class,
        () -> paymentGatewayService.processPayment(payment));
  }

}