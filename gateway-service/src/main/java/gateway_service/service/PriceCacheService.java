package gateway_service.service;

import gateway_service.dto.RawQuoteDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//宣告為 Service Bean
//Controller 會呼叫這個 service 讀取 Redis 快取
@Service
public class PriceCacheService {

	// Redis key prefix
	// Redis 裡的 key 會長這樣：price:NVDA
    private static final String KEY_PREFIX = "price:";

    private final StringRedisTemplate redis;  // Redis 操作物件
    private final ObjectMapper mapper;  // JSON 轉換工具

    public PriceCacheService(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    // 寫入 Redis
    public void put(RawQuoteDTO dto) {
        try {
            String key = KEY_PREFIX + dto.symbol();  // 建立 Redis key
            redis.opsForValue().set(key, mapper.writeValueAsString(dto));  // 將 DTO 轉成 JSON 並存入 Redis
        } catch (Exception e) {
            throw new RuntimeException("Redis put failed", e);
        }
    }
    
    // 取得單一股票價格
    public RawQuoteDTO get(String symbol) {
        try {
        	// 從 Redis 讀取, key = price:NVDA
            String v = redis.opsForValue().get(KEY_PREFIX + normalize(symbol));
            if (v == null) return null;  // 如果沒有資料直接回傳 null
            return mapper.readValue(v, RawQuoteDTO.class);  // JSON → DTO
        } catch (Exception e) {
            throw new RuntimeException("Redis get failed", e);
        }
    }

    // 取得所有股票價格
    public Map<String, RawQuoteDTO> getAll() {
        try {
            Set<String> keys = redis.keys(KEY_PREFIX + "*");  // 取得所有 price:* keys,例如：price:NVDA
            Map<String, RawQuoteDTO> result = new HashMap<>();
            if (keys == null || keys.isEmpty()) return result;

            for (String key : keys) {
                String json = redis.opsForValue().get(key);  // 讀取 JSON
                if (json == null) continue;
                RawQuoteDTO dto = mapper.readValue(json, RawQuoteDTO.class);  // JSON → DTO
                // 放入 map, key = symbol, value = RawQuoteDTO
                result.put(dto.symbol(), dto);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Redis getAll failed", e);
        }
    }

    // Symbol 標準化
    private String normalize(String symbol) {
    	// 去除空白並轉為大寫,如 nvda → NVDA
        return symbol == null ? "" : symbol.trim().toUpperCase();
    }
}
