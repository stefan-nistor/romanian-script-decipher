package ro.uaic.info.romandec.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    private static final String TIMESTAMP = "Timestamp";
    private static final String MESSAGE = "Message";

    @ExceptionHandler({UserAlreadyExistsException.class})
    public ResponseEntity<Object> alreadyExistsException(RuntimeException e){
        Map<String, Object> result = new HashMap<>();
        result.put(TIMESTAMP, LocalDateTime.now());
        result.put(MESSAGE, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({InvalidDataException.class})
    public ResponseEntity<Object> invalidDataException(RuntimeException e){
        Map<String, Object> result = new HashMap<>();
        result.put(TIMESTAMP, LocalDateTime.now());
        result.put(MESSAGE, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NoAvailableDataForGivenInputException.class})
    public ResponseEntity<Object> noAvailableDataForInput(RuntimeException e){
        Map<String, Object> result = new HashMap<>();
        result.put(TIMESTAMP, LocalDateTime.now());
        result.put(MESSAGE, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({HttpClientException.class})
    public ResponseEntity<Object> httpClientException(RuntimeException e){
        Map<String, Object> result = new HashMap<>();
        result.put(TIMESTAMP, LocalDateTime.now());
        result.put(MESSAGE, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
