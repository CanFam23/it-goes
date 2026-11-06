package com.it_goes.api.util.exception;

import com.it_goes.api.payload.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.http.HttpStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(AccountRequestException.class)
    public ResponseEntity<MessageResponse> handleAccountRequest(AccountRequestException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidation(MethodArgumentNotValidException ex) {
        final String field = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "" : ex.getBindingResult().getFieldErrors().getFirst().getField();
        final String message = switch (field) {
            case "email" -> "Enter a valid email address.";
            case "accessCode" -> "Enter an access code.";
            case "username" -> "Enter a username.";
            case "password" -> "Enter a password.";
            case "confirmPassword" -> "Confirm your password.";
            case "firstName", "lastName" -> "Enter your first and last name.";
            case "title" -> "Enter a trip title.";
            case "dateOfTrip" -> "Enter a trip date.";
            case "locationId" -> "Choose a location.";
            case "newLocation.name" -> "Enter a location name.";
            case "newLocation.country" -> "Choose a country.";
            case "body" -> "Write a trip story.";
            default -> "Please check the form fields and try again.";
        };
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatusCode.BAD_REQUEST).body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageResponse> handleBadJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new MessageResponse("Please check the request fields and try again."));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<MessageResponse> handleBadParameter(Exception ex) {
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid or missing request parameter."));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<MessageResponse> handleNotFound(NotFoundException ex) {
        logger.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatusCode.NOT_FOUND).body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleUnexpected(Exception ex) {
        logger.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatusCode.INTERNAL_SERVER_ERROR).body(new MessageResponse("Something went wrong"));
    }
}
