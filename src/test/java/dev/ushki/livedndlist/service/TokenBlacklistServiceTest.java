package dev.ushki.livedndlist.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
public class TokenBlacklistServiceTest {

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private TokenBlacklistService blacklistService;

  @Test
  void testBlacklistToken() {
    String token = "some.jwt.token";
    long ttlMillis = 3600000; // 1 hour
    String expectedKey = "blacklist:" + token;

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    blacklistService.blacklistToken(token, ttlMillis);

    verify(valueOperations).set(eq(expectedKey), eq("revoked"), eq(ttlMillis),
        eq(TimeUnit.MILLISECONDS));
  }
}
