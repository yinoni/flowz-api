package com.flowzapi.flowz_api_builder.security;

import com.flowzapi.flowz_api_builder.exception.RateLimiterException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;
    private final HandlerExceptionResolver resolver;

    // אנחנו מזריקים את ה-handlerExceptionResolver הדיפולטיבי של ספרינג
    public RateLimitingFilter(ProxyManager<String> proxyManager,
                              @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.proxyManager = proxyManager;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String key;
        BucketConfiguration config;

        // שליפת המשתמש הנוכחי משרשרת האבטחה (אם פילטר ה-JWT כבר זיהה אותו)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. הגדרת מדיניות שונה לפי סוג ה-Endpoint באמצעות Bandwidth
        if (path.startsWith("/auth/")) {
            // Endpoints של Auth: מקסימום 5 בקשות בדקה
            key = "rate:auth:" + resolveClientIdentifier(request, auth);
            config = BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(5)
                            .refillIntervally(5, Duration.ofMinutes(1))
                            .build())
                    .build();
        } else {
            // שאר ה-API הכללי: מקסימום 60 בקשות בדקה
            key = "rate:general:" + resolveClientIdentifier(request, auth);
            config = BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(60)
                            .refillIntervally(60, Duration.ofMinutes(1))
                            .build())
                    .build();
        }

        if (proxyManager.builder().build(key, () -> config).tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            // הדלי ריק! חסימת הבקשה והחזרת 429
            resolver.resolveException(request, response, null,
                    new RateLimiterException());
        }
    }

    // מתודת עזר לזיהוי ייחודי: לפי שם המשתמש אם הוא מחובר, או לפי ה-IP שלו אם הוא אנונימי
    private String resolveClientIdentifier(HttpServletRequest request, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName(); // כאן תוכל להמיר ל-CustomUserDetails שלך במידת הצורך (.getId())
        }

        return request.getRemoteAddr();
    }
}