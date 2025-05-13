package com.frederikhandberg.service;

import java.security.KeyPair;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.frederikhandberg.adapter.UserDetailsImpl;
import com.frederikhandberg.exception.InvalidTokenException;
import com.frederikhandberg.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {

    private final KeyStoreManager keyStoreManager;

    @Value("${jwt.expiration.seconds:3600}")
    private long accessTokenExpirationSeconds;

    @Value("${jwt.expiration.refresh:86400}")
    private long refreshTokenExpirationSeconds;

    @Value("${jwt.issuer}")
    private String issuer;

    public JwtService(KeyStoreManager keyStoreManager) {
        this.keyStoreManager = keyStoreManager;
    }

    public String generateToken(User user) {
        return generateToken(new UserDetailsImpl(user));
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpirationSeconds, TokenType.ACCESS);
    }

    public String generateRefreshToken(User user) {
        return generateRefreshToken(new UserDetailsImpl(user));
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpirationSeconds, TokenType.REFRESH);
    }

    public String generateToken(UserDetails userDetails, long expirationSeconds, TokenType tokenType) {
        KeyPair keyPair = keyStoreManager.getCurrentKeyPair();

        Date now = new Date();
        Date expiration = new Date(now.getTime() + (expirationSeconds * 1000));

        return Jwts.builder()
                .issuer(issuer)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .id(UUID.randomUUID().toString())
                .claims(createClaims(userDetails))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS512)
                .compact();
    }

    private Map<String, Object> createClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return claims;
    }

    public boolean validateToken(String token) {
        try {
            KeyPair keyPair = keyStoreManager.getCurrentKeyPair();

            Claims claims = Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            if (TokenType.REFRESH.name().equals(tokenType)) {
                throw new InvalidTokenException("Cannot use refresh token for authentication");
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            KeyPair keyPair = keyStoreManager.getCurrentKeyPair();

            Claims claims = Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            if (!TokenType.REFRESH.name().equals(tokenType)) {
                throw new InvalidTokenException("Not a refresh token");
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        KeyPair keyPair = keyStoreManager.getCurrentKeyPair();

        return Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}

enum TokenType {
    ACCESS,
    REFRESH
}
