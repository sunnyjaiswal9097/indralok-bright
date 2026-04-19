package com.indralokbright.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} | URL: {}", ex.getMessage(), request.getRequestURI());
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", ex.getMessage());
        mav.addObject("url", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at URL: {} | Error: {}", request.getRequestURI(), ex.getMessage(), ex);
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", "An unexpected error occurred. Please try again.");
        mav.addObject("url", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid argument at URL: {} | Error: {}", request.getRequestURI(), ex.getMessage());
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", ex.getMessage());
        mav.addObject("url", request.getRequestURI());
        return mav;
    }
}
