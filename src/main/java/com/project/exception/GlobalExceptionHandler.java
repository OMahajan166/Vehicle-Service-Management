package com.project.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex,
                                  Model model) {

        // Always log the real cause + stack trace — without this the page
        // only shows a vague message and the bug is impossible to diagnose
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        model.addAttribute(
                "errorMessage",
                ex.getMessage());

        return "error";
    }
}