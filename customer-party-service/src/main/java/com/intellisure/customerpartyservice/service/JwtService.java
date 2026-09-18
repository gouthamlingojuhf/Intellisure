package com.intellisure.customerpartyservice.service;


import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(final SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Value("${jwt.expiration}")
    private long expirationTime;

    public String generateToken(UUID userID,String role){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationTime);
        return Jwts.builder()
                .subject(userID.toString())
                .claim("role",role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

}
