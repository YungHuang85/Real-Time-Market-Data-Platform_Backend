package quote_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuoteServiceApplication {

	// Java 程式入口點
	public static void main(String[] args) {
		// 啟動 Spring Boot 應用程式
		// 這行會：
		// 1. 建立 Spring Application Context
		// 2. 載入 application.yml
		// 3. 初始化所有 Bean（Controller / Service / Kafka / WebSocket）
		// 4. 啟動內嵌 Tomcat Server
		SpringApplication.run(QuoteServiceApplication.class, args);
	}

}
