package com.dduru.gildongmu.common.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private int jwtExpirationMs;

    @Value("${jwt.refresh-expiration:2592000000}")
    private long jwtRefreshExpirationMs;

    public String createToken(String userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userId)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String createRefreshToken(String userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtRefreshExpirationMs);

        return Jwts.builder()
                .setSubject(userId)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public Authentication getAuthentication(String token) {
        String userId = getUserIdFromToken(token);
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.error("Access Token이 아닙니다");
                throw new AuthenticationException("Invalid token type") {};
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT 토큰이 만료되었습니다");
            throw new AuthenticationException("Expired JWT token") {};
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰이 유효하지 않습니다");
            throw new AuthenticationException("Invalid JWT token") {};
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = getClaims(refreshToken);
            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                log.error("Refresh Token이 아닙니다");
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Refresh Token이 만료되었습니다");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Refresh Token이 유효하지 않습니다");
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }
}
