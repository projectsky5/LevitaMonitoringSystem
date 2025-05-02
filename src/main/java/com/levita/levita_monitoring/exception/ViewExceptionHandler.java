package com.levita.levita_monitoring.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ViewExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ModelAndView handleHtmlErrors(Exception e, HttpServletRequest req) {
        String acceptHeader = req.getHeader("Accept");
        if(acceptHeader != null && acceptHeader.contains("text/html")) {
            ModelAndView mav = new ModelAndView();
            mav.setViewName("forward:/error.html");
            return mav;
        }
        throw new RuntimeException(e);
    }
}