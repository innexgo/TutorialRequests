package innexgo.hours;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ErrorHandler {
 @ExceptionHandler(value={NoHandlerFoundException.class})
 public ResponseEntity<?> notFoundHandler() {
    return Errors.NOT_FOUND.getResponse();
  }

 @ExceptionHandler(value={Exception.class})
 public ResponseEntity<?> generalHandler() {
    return Errors.UNKNOWN.getResponse();
 }
}
