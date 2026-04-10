package org.example.bookingsystem;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.bookingsystem.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    //генерация токена
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getLogin())
                .claim("role", user.getRole())
                .claim("publicId", user.getPublicId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    //извлечение username из токена
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    //извлечение роли из токена
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // извлечение publicId из токена
    public UUID extractPublicId(String token) {
        String publicIdStr = extractAllClaims(token).get("publicId", String.class);
        return UUID.fromString(publicIdStr);
    }

    //извлечение всех claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //проверка на просрочку
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    //валидация токена
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}