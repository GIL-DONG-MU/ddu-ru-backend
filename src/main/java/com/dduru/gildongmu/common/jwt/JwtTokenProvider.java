package com.dduru.gildongmu.common.jwt;

import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.Role;
import com.dduru.gildongmu.user.repository.UserRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpirationMs;

    public String createToken(Long userId, Role role) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "access")
                .claim(ROLE_CLAIM, role.name())
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

    public Authentication resolveAuthentication(String token, boolean adminApiRequest) {
        if (adminApiRequest) {
            return buildAuthenticationFromDatabase(token);
        }
        return buildAuthenticationFromJwtRoleOrDatabase(token);
    }

    private Authentication buildAuthenticationFromDatabase(String token) {
        Long userId = getUserIdFromToken(token);
        return userRepository.findById(userId)
                .map(User::getRole)
                .map(role -> toAuthentication(userId, role))
                .orElse(null);
    }

    private Authentication buildAuthenticationFromJwtRoleOrDatabase(String token) {
        try {
            Claims claims = getClaims(token);
            String roleStr = claims.get(ROLE_CLAIM, String.class);
            if (roleStr != null && !roleStr.isBlank()) {
                Role role = Role.valueOf(roleStr.trim());
                Long userId = Long.valueOf(claims.getSubject());
                return toAuthentication(userId, role);
            }
        } catch (IllegalArgumentException e) {
            log.warn("JWT role 클레임 처리 실패, DB로 대체합니다: {}", e.getMessage());
        }
        return buildAuthenticationFromDatabase(token);
    }

    private static Authentication toAuthentication(Long userId, Role role) {
        return new UsernamePasswordAuthenticationToken(
                userId.toString(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()))
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

    public String createVerificationToken(Long userId, String phoneNumber) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        String subject = userId != null ? userId.toString() : phoneNumber;

        return Jwts.builder()
                .setSubject(subject)
                .claim("type", "verification")
                .claim("phone_number", phoneNumber)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Verification Token 검증 및 전화번호 추출
     * 
     * @param token verification token
     * @param expectedPhoneNumber 검증할 전화번호
     * @return 검증 성공 여부
     */
    public boolean validateVerificationToken(String token, String expectedPhoneNumber) {
        try {
            Claims claims = getClaims(token);
            String tokenType = claims.get("type", String.class);
            if (!"verification".equals(tokenType)) {
                log.warn("Verification Token이 아닙니다 - {}", tokenType);
                return false;
            }
            
            String phoneNumber = claims.get("phone_number", String.class);
            if (phoneNumber == null || !phoneNumber.equals(expectedPhoneNumber)) {
                log.warn("전화번호가 일치하지 않습니다. 토큰: {}, 요청: {}", phoneNumber, expectedPhoneNumber);
                return false;
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Verification Token이 만료되었습니다");
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
        } catch (JwtException e) {
            log.warn("Verification Token이 유효하지 않습니다: {}", e.getMessage());
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
