package com.artinus.common;

import com.artinus.subscription.domain.ChannelNotAllowedException;
import com.artinus.subscription.domain.ChannelNotFoundException;
import com.artinus.subscription.domain.IllegalStateTransitionException;
import com.artinus.subscription.domain.InvalidPhoneNumberException;
import com.artinus.subscription.service.port.GateUnavailableException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int RETRY_AFTER_SECONDS = 30;

    @ExceptionHandler({
            ChannelNotAllowedException.class,
            IllegalStateTransitionException.class,
            ChannelNotFoundException.class,
            InvalidPhoneNumberException.class
    })
    public ProblemDetail handleDomainValidation(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleRequestBodyValidation(MethodArgumentNotValidException e) {
        List<Violation> violations = e.getBindingResult().getFieldErrors().stream()
                .map(Violation::from)
                .toList();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "요청 본문 검증에 실패했습니다.");
        problem.setProperty("errors", violations);
        return problem;
    }

    @ExceptionHandler(GateUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleGateUnavailable(GateUnavailableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "외부 인증 시스템이 응답하지 않습니다. 잠시 후 다시 시도해 주세요.");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(RETRY_AFTER_SECONDS));

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .headers(headers)
                .body(problem);
    }

    private record Violation(String field, String message) {
        static Violation from(FieldError error) {
            return new Violation(error.getField(), error.getDefaultMessage());
        }
    }
}
