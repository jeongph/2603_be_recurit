package com.artinus.common;

import com.artinus.subscription.domain.ChannelNotAllowedException;
import com.artinus.subscription.domain.ChannelNotFoundException;
import com.artinus.subscription.domain.IllegalStateTransitionException;
import com.artinus.subscription.service.port.GateUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ChannelNotAllowedException.class,
            IllegalStateTransitionException.class,
            ChannelNotFoundException.class,
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class
    })
    public ProblemDetail handleValidation(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(GateUnavailableException.class)
    public ProblemDetail handleGateUnavailable(GateUnavailableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "외부 인증 시스템이 응답하지 않습니다. 잠시 후 다시 시도해 주세요.");
        problem.setProperty("retryAfterSeconds", 30);
        return problem;
    }
}
