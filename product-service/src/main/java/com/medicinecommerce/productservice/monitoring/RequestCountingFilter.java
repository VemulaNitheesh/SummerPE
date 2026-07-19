package com.medicinecommerce.productservice.monitoring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestCountingFilter extends OncePerRequestFilter {

    private final RequestCounter requestCounter;

    public RequestCountingFilter(RequestCounter requestCounter) {
        this.requestCounter = requestCounter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/monitor")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        requestCounter.increment();

        try {
            filterChain.doFilter(request, response);
        } finally {
            requestCounter.decrement();
        }
    }
}
