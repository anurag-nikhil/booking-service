package com.reservation.bookingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    @ExceptionHandler(BusResourceNotFoundException.class)
    public Error handleBRSResourceNotFoundException(BusResourceNotFoundException ex) {
        return new Error(ErrorType.NOT_FOUND_ERROR, ex.getMessage(), LocalDateTime.now(), null);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Error handleBRSException(BusResourceNotFoundException ex) {
        return new Error(ErrorType.SERVER_ERROR, ex.getMessage(), LocalDateTime.now(), null);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    @ExceptionHandler(FieldException.class)
    public Error handleFieldException(FieldException ex) {
        return new Error(ErrorType.FIELD_ERROR, ex.getMessage(), LocalDateTime.now(), null);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
//        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred");
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//    }
}