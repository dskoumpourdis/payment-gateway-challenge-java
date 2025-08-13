package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(EventProcessingException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleException(EventProcessingException ex) {
    LOG.error("EventProcessingException happened", ex);
    return new ErrorResponse("Page not found");
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleConstraintViolationErrors(ConstraintViolationException ex) {
    LOG.error("ConstraintViolationException happened", ex);
    return new ErrorResponse("Validation failed");
  }

}
