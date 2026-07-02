package com.mst.filter;

import com.mst.service.GatewayJwtService;
import com.mst.service.RequiredRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private GatewayJwtService jwtService;
    @Autowired
    private RequiredRoleService requiredRoleService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        if (path.contains("/internal/")) {
            return reject(exchange, HttpStatus.FORBIDDEN);
        }

        String token = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (token == null || !token.startsWith("Bearer ") || !jwtService.isValid(token)) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }
        String username = jwtService.getUsername(token);
        List<String> roles = jwtService.getRoles(token);

        String requiredRole = requiredRoleService.getRequiredRole(
                path,
                exchange.getRequest().getMethod()
        );

        if (!"admin".equalsIgnoreCase(username) && !roles.contains(requiredRole)) {
            return reject(exchange, HttpStatus.FORBIDDEN);
        }

        ServerWebExchange authenticatedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.set("X-User-Id", String.valueOf(jwtService.getUserId(token)));
                    headers.set("X-Username", jwtService.getUsername(token));
                    headers.set("X-User-Roles", String.join(",", roles));
                    headers.set("X-User-Email", String.valueOf(jwtService.getEmail(token)));
                }))
                .build();

        return chain.filter(authenticatedExchange);
    }

    private boolean isPublic(String path) {
        return "/api/auth/signup".equals(path)
                || "/api/auth/signin".equals(path)
                || path.startsWith("/fallback/")
                || path.startsWith("/mst-")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars")
                || path.startsWith("/v3/api-docs");
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
