package com.dduru.gildongmu.common.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpirationMs;

    public String createToken(Long userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtRefreshExpirationMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public Authentication getAuthentication(String token) {
        Long userId = getUserIdFromToken(token);
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
                log.warn("Access Token이 아닙니다 - {}", tokenType);
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰이 만료되었습니다");
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT 형식이 틀렸습니다");
            return false;
        } catch (MalformedJwtException e) {
            log.warn("JWT 구조가 잘못되었습니다");
            return false;
        } catch (SignatureException e) {
            log.warn("JWT 서명 검증 실패하였습니다");
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("부적절한 값이 들어왔습니다");
            return false;
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
