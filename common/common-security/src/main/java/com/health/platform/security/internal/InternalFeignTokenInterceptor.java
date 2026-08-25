package com.health.platform.security.internal;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

/**
 * 服务间 Feign 调用自动携带内部令牌。
 */
public class InternalFeignTokenInterceptor implements RequestInterceptor {

    private final String internalToken;

    public InternalFeignTokenInterceptor(@Value("${INTERNAL_TOKEN:dev-internal-token}") String internalToken) {
        this.internalToken = internalToken;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(InternalTokenFilter.INTERNAL_TOKEN_HEADER, internalToken);
    }
}
