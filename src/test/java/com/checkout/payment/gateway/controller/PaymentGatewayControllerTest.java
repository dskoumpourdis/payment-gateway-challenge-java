package com.checkout.payment.gateway.controller;


import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private PaymentGatewayService paymentGatewayService;

  private PostPaymentRequest validRequest;

  @BeforeEach
  void setUp() {
    validRequest = new PostPaymentRequest();
    validRequest.setCardNumberLastFour("1234567890123456");
    validRequest.setExpiryMonth(12);
    validRequest.setExpiryYear(2025);
    validRequest.setCurrency("USD");
    validRequest.setAmount(1000);
    validRequest.setCvv("123");
  }

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse payment = new PostPaymentResponse();
    UUID uuid = UUID.randomUUID();
    payment.setId(uuid);
    payment.setAmount(10);
    payment.setCurrency("USD");
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2024);
    payment.setCardNumberLastFour("4321");

    when(paymentGatewayService.getPaymentById(any(UUID.class))).thenReturn(payment);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {

    when(paymentGatewayService.getPaymentById(any(UUID.class))).thenThrow( new EventProcessingException("Invalid ID"));

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Page not found"));
  }
  @Test
  void processPayment_ValidRequest_ReturnsAuthorizedResponse() throws Exception {
    // Given
    UUID paymentId = UUID.randomUUID();
    when(paymentGatewayService.processPayment(any(PostPaymentRequest.class)))
        .thenReturn(paymentId);

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amount", is(1000)))
        .andExpect(jsonPath("$.currency", is("USD")))
        .andExpect(jsonPath("$.status", is("Authorized")))
        .andExpect(jsonPath("$.id", is(paymentId.toString())))
        .andExpect(jsonPath("$.expiryMonth", is(12)))
        .andExpect(jsonPath("$.expiryYear", is(2025)))
        .andExpect(jsonPath("$.cardNumberLastFour", is("1234567890123456")));

    verify(paymentGatewayService).processPayment(any(PostPaymentRequest.class));
  }

  @Test
  void processPayment_DeclinedPayment_ReturnsDeclinedResponse() throws Exception {
    // Given
    when(paymentGatewayService.processPayment(any(PostPaymentRequest.class)))
        .thenReturn(null);

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("Declined")))
        .andExpect(jsonPath("$.id", is(nullValue())));
  }

  @Test
  void processPayment_InvalidCardNumber_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCardNumberLastFour("123"); // Too short

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());

    verify(paymentGatewayService, never()).processPayment(any());
  }

  @Test
  void processPayment_CardNumberTooLong_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCardNumberLastFour("12345678901234567890"); // Too long

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_CardNumberWithNonDigits_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCardNumberLastFour("123456789012345a");

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_NullCardNumber_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCardNumberLastFour(null);

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_InvalidExpiryMonth_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setExpiryMonth(13); // Invalid month

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_ZeroExpiryMonth_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setExpiryMonth(0);

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_InvalidCurrency_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCurrency("US"); // Too short

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_NullCurrency_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCurrency(null);

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_InvalidCvv_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCvv("12"); // Too short

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_CvvTooLong_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCvv("12345"); // Too long

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_CvvWithNonDigits_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCvv("12a");

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_NullCvv_ReturnsBadRequest() throws Exception {
    // Given
    validRequest.setCvv(null);

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_EmptyRequestBody_ReturnsBadRequest() throws Exception {
    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_MalformedJson_ReturnsBadRequest() throws Exception {
    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{invalid json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void processPayment_MinimumValidValues_ReturnsOk() throws Exception {
    // Given
    validRequest.setCardNumberLastFour("12345678901234"); // Minimum length
    validRequest.setExpiryMonth(1); // Minimum month
    validRequest.setCurrency("USD"); // 3 chars
    validRequest.setCvv("123"); // 3 digits
    UUID paymentId = UUID.randomUUID();
    when(paymentGatewayService.processPayment(any(PostPaymentRequest.class)))
        .thenReturn(paymentId);

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("Authorized")));
  }

  @Test
  void processPayment_MaximumValidValues_ReturnsOk() throws Exception {
    // Given
    validRequest.setCardNumberLastFour("1234567890123456789"); // Maximum length
    validRequest.setExpiryMonth(12); // Maximum month
    validRequest.setCvv("1234"); // 4 digits
    UUID paymentId = UUID.randomUUID();
    when(paymentGatewayService.processPayment(any(PostPaymentRequest.class)))
        .thenReturn(paymentId);

    // When & Then
    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("Authorized")));
  }
}