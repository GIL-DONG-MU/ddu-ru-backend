package com.dduru.gildongmu.common.jwt;

import com.dduru.gildongmu.common.time.KoreaTime;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtTokenProviderTest {

    private static final String SECRET = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
    private static final Instant NOW = Instant.parse("2026-07-11T03:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, KoreaTime.ZONE_ID);
    private static final int ACCESS_EXPIRATION_MS = 60_000;

    private JwtTokenProvider createProvider() {
        JwtTokenProvider provider = new JwtTokenProvider(
                mock(UserRepository.class),
                new TimeProvider(FIXED_CLOCK)
        );
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", ACCESS_EXPIRATION_MS);
        return provider;
    }

    @Test
    void 인증토큰_주체는_userId_클레임은_전화번호() {
        // given
        JwtTokenProvider provider = createProvider();

        // when
        String token = provider.createVerificationToken(42L, "01012345678");

        // then
        Claims claims = parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("phone_number", String.class)).isEqualTo("01012345678");
        assertThat(claims.get("type", String.class)).isEqualTo("verification");
        assertThat(claims.getIssuedAt()).isEqualTo(Date.from(NOW));
        assertThat(claims.getExpiration()).isEqualTo(Date.from(NOW.plusMillis(ACCESS_EXPIRATION_MS)));
    }

    @Test
    void 인증토큰_userId없으면_subject를_전화번호로_대체() {
        // given
        JwtTokenProvider provider = createProvider();

        // when
        String token = provider.createVerificationToken(null, "01099998888");

        // then
        Claims claims = parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("01099998888");
        assertThat(claims.get("phone_number", String.class)).isEqualTo("01099998888");
        assertThat(claims.get("type", String.class)).isEqualTo("verification");
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setClock(() -> Date.from(NOW.plusSeconds(30)))
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}
