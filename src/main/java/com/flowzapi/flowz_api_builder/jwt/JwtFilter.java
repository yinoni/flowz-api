package com.flowzapi.flowz_api_builder.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // שירות שלך ששולף משתמש מה-MongoDB לפי username/email

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.equals("/auth/refresh")
                || path.equals("/auth/login")
                || path.equals("/auth/register")
                || path.equals("/auth/google")
                || path.equals("/auth/logout");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if(token != null) {
            try{
                String email = jwtService.validateTokenAndGetEmail(token);
                String requestDispatcherPath = request.getServletPath();
                boolean isAuthPath = requestDispatcherPath.startsWith("/auth/");

                Boolean verifiedClaim = jwtService.extractClaim(token, "verified", Boolean.class);
                boolean isVerified = (verifiedClaim != null && verifiedClaim) || isAuthPath;

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null && isVerified) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            catch(Exception e){
                logger.error(e.getMessage());
            }

        }

        filterChain.doFilter(request, response);
    }
}
