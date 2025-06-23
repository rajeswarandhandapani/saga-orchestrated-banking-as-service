package com.rajeswaran.apigateway.config;

import com.rajeswaran.apigateway.service.client.AdminDashboardClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class AdminDashboardClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public AdminDashboardClient adminDashboardClient(WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder
                .defaultHeader("Content-Type", "application/json")
                .build();
        
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AdminDashboardClient.class);
    }
}
