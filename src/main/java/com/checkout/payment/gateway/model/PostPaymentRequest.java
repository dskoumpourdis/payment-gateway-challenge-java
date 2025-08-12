package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

public class PostPaymentRequest implements Serializable {

  @JsonProperty("card_number")
  @Size(min = 14, max = 19)
  @Pattern(regexp = "\\d+")
  @NotNull
  private String cardNumberLastFour;
  @JsonProperty("expiry_month")
  @Size(min = 1, max = 12)
  @NotNull
  private int expiryMonth;
  @NotNull
  @JsonProperty("expiry_year")
  @NotNull
  private int expiryYear;
  @Size(min = 3, max = 3)
  @NotNull
  private String currency;
  @NotNull
  private int amount;
  @Size(min = 3, max = 4)
  @Pattern(regexp = "\\d+")
  @NotNull
  private int cvv;

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

  public int getCvv() {
    return cvv;
  }

  public void setCvv(int cvv) {
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
