package com.intellisure.customerpartyservice.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecretKey secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    public String generateToken(
            UUID userId,
            String role,
            UUID customerId
    ) {
        Date now = new Date();
        Date expiry =
                new Date(now.getTime() + expirationTime);

        String normalizedRole =
                normalizeRole(role);

        var tokenBuilder = Jwts.builder()
                .subject(userId.toString())

                // Keep singular role for backward compatibility.
                .claim("role", normalizedRole)

                // Standard role list consumed by other services.
                .claim(
                        "roles",
                        List.of(normalizedRole)
                )

                .issuedAt(now)
                .expiration(expiry)
                .signWith(
                        secretKey,
                        Jwts.SIG.HS256
                );

        /*
         * customerId is required for POLICYHOLDER ownership checks.
         * Internal users such as UNDERWRITER or ADMIN do not need it.
         */
        if (customerId != null) {
            tokenBuilder.claim(
                    "customerId",
                    customerId.toString()
            );
        }

        return tokenBuilder.compact();
    }

    public long getExpirationSeconds() {
        return expirationTime / 1000;
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "User role is required for JWT creation"
            );
        }

        String normalized =
                role.trim().toUpperCase();

        if (normalized.startsWith("ROLE_")) {
            return normalized.substring(5);
        }

        return normalized;
    }
}