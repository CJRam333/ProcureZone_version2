package com.nslindia.procurezone.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.debug("JwtAuthenticationFilter: Processing request to {}", requestURI);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("JwtAuthenticationFilter: No existing authentication in SecurityContext");

            String token = resolveBearerToken(request);
            logger.debug("JwtAuthenticationFilter: Token extracted from header: {}",
                    token != null ? "YES (length=" + token.length() + ")" : "NO");

            if (token != null) {
                // Check if token is blacklisted (user logged out)
                boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
                logger.debug("JwtAuthenticationFilter: Token blacklisted: {}", isBlacklisted);

                if (isBlacklisted) {
                    // Token is blacklisted, don't authenticate
                    logger.debug("JwtAuthenticationFilter: Token is blacklisted, skipping authentication");
                    filterChain.doFilter(request, response);
                    return;
                }

                logger.debug("JwtAuthenticationFilter: Attempting to parse access token");
                try {
                    var parseResult = jwtService.parseAccessToken(token);
                    logger.debug("JwtAuthenticationFilter: Parse result present: {}", parseResult.isPresent());

                    parseResult.ifPresent(payload -> {
                        logger.debug("JwtAuthenticationFilter: Token parsed successfully for user: {}",
                                payload.username());

                        UserPrincipal principal = new UserPrincipal(
                                payload.userId(),
                                payload.employeeNumber(),
                                payload.employeeId(),
                                payload.username(),
                                payload.displayName(),
                                payload.email(),
                                payload.roles(),
                                payload.canView(),
                                payload.canAdd(),
                                payload.canEdit(),
                                payload.canDelete());

                        logger.debug("JwtAuthenticationFilter: Creating principal with roles: {}", payload.roles());
                        logger.debug("JwtAuthenticationFilter: Authorities from principal: {}",
                                principal.authorities());

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.authorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        logger.debug(
                                "JwtAuthenticationFilter: Authentication set in SecurityContext for user: {} with authorities: {}",
                                payload.username(), authentication.getAuthorities());
                    });

                    if (parseResult.isEmpty()) {
                        logger.warn("JwtAuthenticationFilter: Token invalid or expired for URI: {}", requestURI);
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                                "{\"code\":\"UNAUTHORIZED\",\"message\":\"Token is invalid or expired\"}");
                        return;
                    }
                } catch (Exception e) {
                    logger.error("JwtAuthenticationFilter: Exception during token parsing for URI: " + requestURI, e);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"code\":\"UNAUTHORIZED\",\"message\":\"Token is invalid or expired\"}");
                    return;
                }
            } else {
                logger.debug("JwtAuthenticationFilter: No Bearer token in request to {}", requestURI);
            }
        } else {
            logger.debug("JwtAuthenticationFilter: Authentication already exists in SecurityContext");
        }

        logger.debug("JwtAuthenticationFilter: Proceeding with filter chain for {}", requestURI);
        filterChain.doFilter(request, response);
    }

    @Nullable
    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
