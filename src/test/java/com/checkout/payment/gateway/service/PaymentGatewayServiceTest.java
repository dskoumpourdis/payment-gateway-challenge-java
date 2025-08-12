package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpServerErrorException;

@SpringBootTest
class PaymentGatewayServiceTest {
  @Autowired
  PaymentGatewayService paymentGatewayService;

  @Test
  void whenCardNumberEndInOddPaymentIsAuthorized(){
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumberLastFour("4321");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2026);
    payment.setCurrency("USD");
    payment.setAmount(10);
    payment.setCvv("999");

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

    assertThrows(
        HttpServerErrorException.class,
        () -> paymentGatewayService.processPayment(payment));
  }

}