package at.technikum_wien.swen3.paperless.controller;

import at.technikum_wien.swen3.paperless.exception.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<String> handleStorageException(StorageException ex) {
        // If storage error -> return 503 Service Unavailable
        return new ResponseEntity<>("File storage service is currently unavailable.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        // If validation error -> return a 400 Bad Request
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
