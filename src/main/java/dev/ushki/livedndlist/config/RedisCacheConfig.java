package dev.ushki.livedndlist.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisCacheConfig {

  @Bean("cacheObjectMapper")
  public ObjectMapper cacheObjectMapper() {
    PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("dev.ushki.livedndlist.")
        .allowIfSubType("org.springframework.data.domain.")
        .allowIfSubType("com.fasterxml.jackson.databind.")
        .allowIfSubType("java.util.")
        .build();

    return JsonMapper.builder()
        .findAndAddModules()
        .activateDefaultTyping(validator, ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY)
        .build();
  }
}