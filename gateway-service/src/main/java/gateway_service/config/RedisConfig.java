package gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration  // 表示此類別為 Spring Configuration,Spring Boot 啟動時會載入並建立 Bean
public class RedisConfig {

    @Bean  // 定義一個 Spring Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);  // 建立 Redis Template,這個 template 用來操作 Redis key/value
    }
}
