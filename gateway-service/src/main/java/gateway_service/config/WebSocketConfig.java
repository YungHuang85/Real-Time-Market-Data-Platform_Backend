package gateway_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration  // 宣告為 Spring Configuration class
@EnableWebSocketMessageBroker  // 啟用 Spring WebSocket Message Broker（STOMP 架構）,Spring Boot 會建立 WebSocket server 與 message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 註冊 WebSocket endpoint
    // 前端會連線到這個 endpoint
    // 例如：
    // ws://localhost:8080/ws
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 純 WS（前端用 ws://localhost:8080/ws）
        registry.addEndpoint("/ws")
		        // 允許所有來源連線 WebSocket
		        // 開發環境常用
		        // production 建議限制 origin
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 啟用內建 Simple Broker
        // 用來處理 publish / subscribe
        // 前端訂閱：
        // /topic/xxx
    	registry.enableSimpleBroker("/topic");
        // 設定 application prefix
        // 前端 send message 時會使用：
        // /app/xxx
        // Spring Controller 會處理
        registry.setApplicationDestinationPrefixes("/app");
    }
}