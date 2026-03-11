package gateway_service.service;

import gateway_service.dto.RawQuoteDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service  // 宣告為 Spring Service
public class QuoteConsumer {

    private final ObjectMapper mapper;  // JSON 轉換工具
    private final PriceCacheService cache;  // 用來儲存最新價格
    private final SimpMessagingTemplate ws;  // 用來推送資料到前端

    public QuoteConsumer(ObjectMapper mapper,
                         PriceCacheService cache,
                         SimpMessagingTemplate ws) {
        this.mapper = mapper;
        this.cache = cache;
        this.ws = ws;
    }

    // Kafka consumer
    // 監聽 topic: stock.raw
    // concurrency=3 表示建立 3 個 consumer thread
    @KafkaListener(topics = "stock.raw", concurrency = "3")
    public void onMessage(String message) {
        try {
        	// 將 Kafka message JSON 轉為 DTO
            RawQuoteDTO dto = mapper.readValue(message, RawQuoteDTO.class);
            
            // 過濾非法資料
            if (dto.price() <= 0 || dto.symbol() == null || dto.symbol().isBlank()) {
                return;
            }
            
            cache.put(dto);  // 將最新價格寫入 Redis cache

            // WebSocket push
            // 推送單一股票,前端可以訂閱：/topic/price/NVDA
            ws.convertAndSend("/topic/price/" + dto.symbol(), dto);

            // 推送全部行情,前端可一次訂閱所有股票
            ws.convertAndSend("/topic/price", dto);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}