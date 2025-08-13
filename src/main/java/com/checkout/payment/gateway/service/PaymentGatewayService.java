package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.Currency;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.PostAuthResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static com.checkout.payment.gateway.enums.PaymentStatus.AUTHORIZED;
import static com.checkout.payment.gateway.enums.PaymentStatus.DECLINED;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);
  private static final String BANK_URL = "http://localhost:8080/payments";

  private final PaymentsRepository paymentsRepository;
  private final RestTemplate restTemplate;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, RestTemplate restTemplate) {
    this.paymentsRepository = paymentsRepository;
    this.restTemplate = restTemplate;
  }

  private static PostPaymentResponse getPostPaymentResponse(PostPaymentRequest paymentRequest,
      UUID uuid) {
    PostPaymentResponse postPaymentResponse = new PostPaymentResponse();
    postPaymentResponse.setAmount(paymentRequest.getAmount());
    postPaymentResponse.setCurrency(paymentRequest.getCurrency());
    postPaymentResponse.setStatus(uuid.toString().isEmpty() ? DECLINED : AUTHORIZED);
    postPaymentResponse.setId(uuid);
    postPaymentResponse.setExpiryMonth(paymentRequest.getExpiryMonth());
    postPaymentResponse.setExpiryYear(paymentRequest.getExpiryYear());
    postPaymentResponse.setCardNumberLastFour(paymentRequest.getCardNumberLastFour());
    return postPaymentResponse;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public UUID processPayment(PostPaymentRequest paymentRequest) {

    UUID uuid = null;
    LocalDate currentDate = LocalDate.now();

    if (currentDate.getYear() > paymentRequest.getExpiryYear()) {
      LOG.warn("Invalid year {}", paymentRequest);
      return uuid;
    }
    if (currentDate.getMonthValue() > paymentRequest.getExpiryMonth()) {
      LOG.warn("Invalid month {}", paymentRequest);
      return uuid;
    }

    if (!paymentRequest.getCurrency().equals(Currency.EUR.toString())
        && !paymentRequest.getCurrency().equals(Currency.USD.toString())
        && !paymentRequest.getCurrency().equals(Currency.GBP.toString())) {
      LOG.warn("Invalid currency {}", paymentRequest);
      return uuid;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PostPaymentRequest> requestEntity = new HttpEntity<>(paymentRequest, headers);

    try {
      ResponseEntity<PostAuthResponse> response = restTemplate.postForEntity(
          BANK_URL, requestEntity, PostAuthResponse.class
      );

      if (response != null
          && response.getStatusCode().is2xxSuccessful()
          && response.getBody() != null
          && !response.getBody().getAuthorization_code().isEmpty()) {

        uuid = UUID.fromString(response.getBody().getAuthorization_code());
        paymentRequest.setCardNumberLastFour(
            paymentRequest.getCardNumberLastFour()
                .substring(paymentRequest.getCardNumberLastFour().length() - 4)
        );
        PostPaymentResponse postPaymentResponse = getPostPaymentResponse(paymentRequest, uuid);
        LOG.info("Saving payment with ID {}", uuid);
        paymentsRepository.add(postPaymentResponse);
      }

    } catch (HttpServerErrorException e) {
      LOG.error("Bank simulator returned a server error: {}", e.getMessage());
      throw new EventProcessingException("Bank service unavailable");
    }

    return uuid;
  }
}
