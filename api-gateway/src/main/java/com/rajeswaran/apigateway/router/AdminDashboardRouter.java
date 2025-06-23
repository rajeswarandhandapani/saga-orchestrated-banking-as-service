package com.rajeswaran.apigateway.router;

import com.rajeswaran.apigateway.handler.AdminDashboardHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class AdminDashboardRouter {

    @Bean
    public RouterFunction<ServerResponse> adminDashboardRoute(AdminDashboardHandler adminDashboardHandler) {
        return RouterFunctions.route(
                RequestPredicates.GET("/api/admin-dashboard")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                adminDashboardHandler::fetchAdminDashboard);
    }
}
