package quote_service.service;

import quote_service.dto.RawQuoteDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service  // 讓 Spring Boot 在啟動時自動建立此 Service
public class QuoteProducer {

	// KafkaTemplate 是 Spring Kafka 提供的工具
    // <String, String> 代表 key 與 message 都使用 String 型別
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;  // Jackson JSON 轉換器，用於將 DTO 轉換為 JSON 字串

    public QuoteProducer(KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;  // 注入 Kafka producer
        this.mapper = mapper;  // 注入 JSON mapper
    }

    // 發送即時行情到 Kafka
    public void send(RawQuoteDTO dto) {

        try {
        	// 將 RawQuoteDTO 物件轉換為 JSON 字串
            // Kafka message 通常會使用 JSON 傳輸
            String json = mapper.writeValueAsString(dto);

            kafkaTemplate.send(
                    "stock.raw",  // Kafka topic 名
                    dto.symbol(),  // Kafka message key（通常用股票代號）
                    json  // Kafka message body
            );

        } catch (Exception e) {
            e.printStackTrace();  // 若發送失敗印出錯誤
        }
    }
}