package integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.checkout.payment.gateway.PaymentGatewayApplication;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PostAuthResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

@SpringBootTest(
    classes = PaymentGatewayApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentGatewayIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  private String baseUrl() {
    return "http://localhost:" + port + "/payment";
  }

  @Test
  void shouldCreateAndRetrievePayment() {
    // Create
    PostPaymentRequest request = validRequest();
    ResponseEntity<PostPaymentResponse> createResponse = restTemplate.postForEntity(
        baseUrl(), request, PostPaymentResponse.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertNotNull(createResponse.getBody());
    String paymentId = String.valueOf(createResponse.getBody().getId());

    // Retrieve
    ResponseEntity<GetPaymentResponse> getResponse = restTemplate.getForEntity(
        baseUrl() + "/" + paymentId, GetPaymentResponse.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertNotNull(getResponse.getBody());
    assertThat(getResponse.getBody().getAmount()).isEqualTo(request.getAmount());
    assertThat(getResponse.getBody().getCurrency()).isEqualTo(request.getCurrency());
  }

  @Test
  void shouldRejectInvalidCardNumber() {
    PostPaymentRequest request = validRequest();
    request.setCardNumberLastFour("123456"); // invalid

    ResponseEntity<PostAuthResponse> response = restTemplate.postForEntity(
        baseUrl(), request, PostAuthResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldRejectUnsupportedCurrency() {
    PostPaymentRequest request = validRequest();
    request.setCurrency("XYZ");

    ResponseEntity<PostPaymentResponse> response = restTemplate.postForEntity(
        baseUrl(), request, PostPaymentResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertNotNull(response.getBody());
    assertThat(response.getBody().getStatus().toString()).isEqualTo("DECLINED");
  }

  @Test
  void shouldReturnNotFoundForMissingPayment() {
    ResponseEntity<PostPaymentResponse> response = restTemplate.getForEntity(
        baseUrl() + "/755917bf-7931-4831-967f-ea3c72490d60", PostPaymentResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldRejectMissingRequiredField() {
    PostPaymentRequest request = validRequest();
    request.setCvv(null); // Missing field

    ResponseEntity<PostAuthResponse> response = restTemplate.postForEntity(
        baseUrl(), request, PostAuthResponse.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldApprovePaymentWhenCardEndsWithOddDigit() {
    PostPaymentRequest request = validRequest();
    request.setCardNumberLastFour("4111111111111111"); // ends in 1

    ResponseEntity<PostPaymentResponse> response = restTemplate.postForEntity(
        baseUrl(), request, PostPaymentResponse.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertNotNull(response.getBody());
    assertThat(response.getBody().getStatus().toString()).isEqualTo("AUTHORIZED");
    assertThat(response.getBody().getId().toString()).isNotBlank();
  }

  @Test
  void shouldDeclinePaymentWhenCardEndsWithEvenDigit() {
    PostPaymentRequest request = validRequest();
    request.setCardNumberLastFour("4111111111111112"); // ends in 2

    ResponseEntity<PostPaymentResponse> response = restTemplate.postForEntity(
        baseUrl(), request, PostPaymentResponse.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertNotNull(response.getBody());
    assertThat(response.getBody().getStatus().toString()).isEqualTo("DECLINED");
  }

  @Test
  void shouldReturnServiceUnavailableWhenCardEndsWithZero() {
    PostPaymentRequest request = validRequest();
    request.setCardNumberLastFour("4111111111111110"); // ends in 0

    assertThrows(
        RestClientException.class,
        () -> restTemplate.postForEntity(baseUrl(), request, ErrorResponse.class));
  }

  private PostPaymentRequest validRequest() {
    PostPaymentRequest req = new PostPaymentRequest();
    req.setAmount(1000);
    req.setCurrency("USD");
    req.setCardNumberLastFour("4111111111111111");
    req.setExpiryMonth(12);
    req.setExpiryYear(2030);
    req.setCvv("123");
    return req;
  }
}
