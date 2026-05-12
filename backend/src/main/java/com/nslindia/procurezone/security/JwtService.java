package com.nslindia.procurezone.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.nslindia.procurezone.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey secretKey;
    private final Clock clock;

    public JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.secretKey = initKey(properties.getSecret());
    }

    public AccessToken generateAccessToken(UserPrincipal principal) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.getAccessToken().getExpiration());
        String token = Jwts.builder()
                .subject(principal.username())
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("uid", principal.userId())
                .claim("emp", principal.employeeNumber())
                .claim("empId", principal.employeeId())
                .claim("name", principal.displayName())
                .claim("email", principal.email())
                .claim("roles", principal.roles())
                .claim("canView", principal.canView())
                .claim("canAdd", principal.canAdd())
                .claim("canEdit", principal.canEdit())
                .claim("canDelete", principal.canDelete())
                .signWith(secretKey)
                .compact();
        return new AccessToken(token, issuedAt, expiresAt);
    }

    public Optional<JwtPayload> parseAccessToken(String token) {
        try {
            Jws<Claims> parsed = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            Claims claims = parsed.getPayload();
            Instant issuedAt = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null;
            Instant expiresAt = claims.getExpiration() != null ? claims.getExpiration().toInstant() : null;
            Set<String> roles = extractRoles(claims);
            JwtPayload payload = new JwtPayload(
                    claims.getSubject(),
                    extractLong(claims, "uid"),
                    extractInteger(claims, "emp"),
                    claims.get("empId", String.class),
                    claims.get("name", String.class),
                    claims.get("email", String.class),
                    roles,
                    extractBoolean(claims, "canView"),
                    extractBoolean(claims, "canAdd"),
                    extractBoolean(claims, "canEdit"),
                    extractBoolean(claims, "canDelete"),
                    issuedAt,
                    expiresAt);
            return Optional.of(payload);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private Long extractLong(Claims claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    private Integer extractInteger(Claims claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Number n) {
            return n.intValue();
        }
        return null;
    }

    private boolean extractBoolean(Claims claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return "true".equalsIgnoreCase(s) || "1".equals(s);
        }
        return false;
    }

    private SecretKey initKey(String value) {
        Assert.hasText(value, "JWT secret must be configured");
        byte[] keyBytes = value.getBytes(StandardCharsets.UTF_8);
        try {
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (WeakKeyException ex) {
            throw new IllegalStateException("JWT secret must be at least 256 bits (32 bytes)", ex);
        }
    }

    private Set<String> extractRoles(Claims claims) {
        Object raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            Set<String> roles = new LinkedHashSet<>();
            for (Object element : list) {
                if (element != null) {
                    roles.add(element.toString());
                }
            }
            return roles;
        }
        return Set.of();
    }

    public record AccessToken(String token, Instant issuedAt, Instant expiresAt) {
    }

    public record JwtPayload(
            String username,
            Long userId,
            Integer employeeNumber,
            String employeeId,
            String displayName,
            String email,
            Set<String> roles,
            boolean canView,
            boolean canAdd,
            boolean canEdit,
            boolean canDelete,
            Instant issuedAt,
            Instant expiresAt) {
    }
}
