package com.rajeswaran.apigateway.router;

import com.rajeswaran.apigateway.handler.AdminCompositeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class AdminDashboardRouter {

    @Bean
    public RouterFunction<ServerResponse> routes(AdminCompositeHandler adminCompositeHandler) {
        return RouterFunctions.route(
                RequestPredicates.GET("/api/admin-dashboard"),
                adminCompositeHandler::getAdminDashboard
        );
    }
}
