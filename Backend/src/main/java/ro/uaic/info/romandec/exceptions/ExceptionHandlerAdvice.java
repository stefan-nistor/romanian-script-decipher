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

    @ExceptionHandler({UserAlreadyExistsException.class})
    public ResponseEntity<Object> alreadyExistsException(RuntimeException e){
        Map<String, Object> result = new HashMap<>();
        result.put("Timestamp", LocalDateTime.now());
        result.put("Message", e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.NOT_ACCEPTABLE);
    }

}
