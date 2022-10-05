package com.zenika.tutoriels.observabilitydemo.logger.utils;

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@AutoConfiguration
public class ServiceLoggerAutoConfiguration {

    @Bean
    @Profile("requests-attributes-in-mdc")
    public OncePerRequestFilter addRequestInContext(){
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                MDC.put("request.method", request.getMethod());
                MDC.put("request.uri", request.getRequestURI());
                chain.doFilter(request, response);
                MDC.remove("request.method");
                MDC.remove("request.uri");
            }
        };
    }

}
