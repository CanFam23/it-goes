package com.it_goes.api.util.exception;

import com.it_goes.api.payload.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
            default -> "Please check the form fields and try again.";
        };
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatusCode.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException ex) {
        logger.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatusCode.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception ex) {
        logger.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatusCode.INTERNAL_SERVER_ERROR).body("Something went wrong");
    }
}
