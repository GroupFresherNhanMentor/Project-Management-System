package fpt.qn.project_management_system.common.exception;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {

        // 1. Tạo một đối tượng ProblemDetail với trạng thái BAD_REQUEST
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, // Status 400
                "Dữ liệu đầu vào không hợp lệ. Vui lòng kiểm tra lại." // Detail message tổng quát
        );

        // 2. (Tùy chọn nhưng nên làm) Thiết lập một type cụ thể cho lỗi validation
        problemDetail.setType(URI.create("https://api.iuh.fit.se/errors/validation-failed"));
        // 3. Thiết lập title
        problemDetail.setTitle("Lỗi Validation");
        // 4. (Tùy chọn) Thiết lập một instance cụ thể cho lỗi này
        problemDetail.setInstance(URI.create(request.getContextPath() + request.getDescription(false)));
        // 5. Thiết lập timestamp
        problemDetail.setProperty("timestamp", Instant.now());

        // 6. QUAN TRỌNG: Thêm các lỗi validation chi tiết vào một property "errors"
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problemDetail.setProperty("errors", errors); // Đây là nơi chứa danh sách lỗi chi tiết

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ProblemDetail> handleEntityNotFoundException(
            Exception ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage());
        problemDetail.setTitle("Internal Server Error");
        log.error("Internal Server Error: {}", ex.getMessage());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ProblemDetail> handleCustomException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("CustomException");
        log.error("Bad Request: {}", ex.getMessage());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Bad Request");
        log.error("Bad Request: {}", ex.getMessage());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ProblemDetail> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage());
        problemDetail.setTitle("Internal Server Error");
        log.error("Internal Server Error: {}", ex.getMessage());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<ProblemDetail> handleNotFoundException(
            IllegalStateException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Bad Request");
        log.error("Bad Request: {}", ex.getMessage());
        return ResponseEntity.of(problemDetail).build();
    }

}
