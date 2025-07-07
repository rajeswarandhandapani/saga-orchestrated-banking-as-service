package com.rajeswaran.common.http;

import com.rajeswaran.common.AppConstants;
import com.rajeswaran.common.util.CorrelationIdMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * HTTP client interceptor that automatically adds correlation ID to outgoing HTTP requests.
 * Use this with RestTemplate or WebClient for service-to-service HTTP calls.
 */
@Slf4j
public class CorrelationIdHttpInterceptor implements ClientHttpRequestInterceptor {

    @Override
    @NonNull
    public ClientHttpResponse intercept(
            @NonNull HttpRequest request, 
            @NonNull byte[] body, 
            @NonNull ClientHttpRequestExecution execution) throws IOException {
        
        // Check if correlation ID already exists in request headers
        String existingCorrelationId = request.getHeaders().getFirst(AppConstants.CORRELATION_ID_HEADER);
        
        if (!StringUtils.hasText(existingCorrelationId)) {
            // Get correlation ID from current MDC context
            String correlationId = CorrelationIdMessageUtils.getCurrentCorrelationId();
            
            if (StringUtils.hasText(correlationId)) {
                // Add correlation ID to outgoing HTTP request
                request.getHeaders().add(AppConstants.CORRELATION_ID_HEADER, correlationId);
                log.debug("Added correlation ID {} to outgoing HTTP request to {}", 
                         correlationId, request.getURI());
            } else {
                log.warn("No correlation ID found in MDC for outgoing HTTP request to {}", 
                        request.getURI());
            }
        } else {
            log.debug("Correlation ID already exists in HTTP request headers: {}", existingCorrelationId);
        }
        
        return execution.execute(request, body);
    }
}
