package com.health.platform.gateway.filter;

import com.health.platform.core.constant.SecurityConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String existing = exchange.getRequest().getHeaders().getFirst(SecurityConstants.REQUEST_ID_HEADER);
        final String requestId = (existing == null || existing.isBlank())
                ? UUID.randomUUID().toString().replace("-", "")
                : existing;
        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder.header(SecurityConstants.REQUEST_ID_HEADER, requestId))
                .build();
        mutated.getResponse().getHeaders().set(SecurityConstants.REQUEST_ID_HEADER, requestId);
        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
