package com.dduru.gildongmu.common.jwt;

import com.dduru.gildongmu.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtTokenProviderTest {

    private static final String SECRET = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";

    private JwtTokenProvider createProvider() {
        JwtTokenProvider provider = new JwtTokenProvider(mock(UserRepository.class));
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 60_000);
        return provider;
    }

    @Test
    void 인증토큰_주체는_userId_클레임은_전화번호() {
        // given
        JwtTokenProvider provider = createProvider();

        // when
        String token = provider.createVerificationToken(42L, "01012345678");

        // then
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("phone_number", String.class)).isEqualTo("01012345678");
        assertThat(claims.get("type", String.class)).isEqualTo("verification");
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void 인증토큰_userId없으면_subject를_전화번호로_대체() {
        // given
        JwtTokenProvider provider = createProvider();

        // when
        String token = provider.createVerificationToken(null, "01099998888");

        // then
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("01099998888");
        assertThat(claims.get("phone_number", String.class)).isEqualTo("01099998888");
        assertThat(claims.get("type", String.class)).isEqualTo("verification");
    }
}
