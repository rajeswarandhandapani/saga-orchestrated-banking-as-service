package com.rajeswaran.common.config;

import com.rajeswaran.common.messaging.CorrelationIdChannelInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 * Auto-configuration for correlation ID handling in Spring Cloud Stream.
 * Automatically registers the correlation ID channel interceptor for all message channels.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(ChannelInterceptor.class)
public class CorrelationIdAutoConfiguration {

    /**
     * Creates and registers the correlation ID channel interceptor as a global interceptor.
     * This will automatically handle correlation ID for all Spring Cloud Stream channels.
     *
     * @return the correlation ID channel interceptor
     */
    @Bean
    @ConditionalOnMissingBean
    @GlobalChannelInterceptor
    public CorrelationIdChannelInterceptor correlationIdChannelInterceptor() {
        log.info("Registering CorrelationIdChannelInterceptor for automatic correlation ID handling");
        return new CorrelationIdChannelInterceptor();
    }
}
