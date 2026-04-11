package dev.ushki.livedndlist.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

  @Autowired
  private StringRedisTemplate redisTemplate;

  private static final String BLACKLIST_PREFIX = "blacklist:";

  public void blacklistToken(String token, long expirationMillis) {
    String key = BLACKLIST_PREFIX + token;
    redisTemplate.opsForValue().set(key, "revoked", expirationMillis, TimeUnit.MILLISECONDS);
  }

  public boolean isTokenBlacklisted(String token) {
    String key = BLACKLIST_PREFIX + token;
    return redisTemplate.hasKey(key);
  }
}
