package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

public class PostPaymentRequest implements Serializable {

  @JsonProperty("card_number")
  @Size(min = 14, max = 19, message = "Card number must be between 14 and 19 digits")
  @Pattern(regexp = "\\d+", message = "Card number must contain only digits")
  @NotNull(message = "Card number is required")
  private String cardNumberLastFour;

  @JsonProperty("expiry_month")
  @Min(value = 1, message = "Expiry month must be between 1 and 12")
  @Max(value = 12, message = "Expiry month must be between 1 and 12")
  private int expiryMonth;

  @JsonProperty("expiry_year")
  @Min(value = 2025, message = "Expiry year must be current year or later")
  private int expiryYear;

  @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
  @NotNull(message = "Currency is required")
  private String currency;

  @Min(value = 1, message = "Amount must be greater than 0")
  private int amount;

  @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
  @Pattern(regexp = "\\d+", message = "CVV must contain only digits")
  @NotNull(message = "CVV is required")
  private String cvv;

  public String getCardNumberLastFour() {
    return cardNumberLastFour;
  }

  public void setCardNumberLastFour(String cardNumberLastFour) {
    this.cardNumberLastFour = cardNumberLastFour;
  }

  public int getExpiryMonth() {
    return expiryMonth;
  }

  public void setExpiryMonth(int expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public int getExpiryYear() {
    return expiryYear;
  }

  public void setExpiryYear(int expiryYear) {
    this.expiryYear = expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getCvv() {
    return cvv;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }

  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumberLastFour=" + cardNumberLastFour +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }
}
