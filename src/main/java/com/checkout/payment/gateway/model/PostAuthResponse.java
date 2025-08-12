package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostAuthResponse {
  private String authorized;
  @JsonProperty("authorization_code")
  private String authorizationCode;

  public String getAuthorized() {
    return authorized;
  }

  public void setAuthorized(String authorized) {
    this.authorized = authorized;
  }

  public String getAuthorization_code() {
    return authorizationCode;
  }

  public void setAuthorization_code(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }
}
