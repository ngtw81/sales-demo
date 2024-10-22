package com.demo.gamesales.upload;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(String.format("Missing parameter with name:%s", ex.getParameterName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleInternalServerError(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body("Internal Server Encountered");
    }
}
