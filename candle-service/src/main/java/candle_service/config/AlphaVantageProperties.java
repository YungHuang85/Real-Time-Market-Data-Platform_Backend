package candle_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component  // 宣告為 Spring Bean
//將 application.yml 中 prefix = alphavantage 的設定,自動綁定到此類別的屬性, 例如：
//alphavantage: key: xxxxx
@ConfigurationProperties(prefix = "alphavantage")  
public class AlphaVantageProperties {

    private String key;  // AlphaVantage API Key,用來呼叫 AlphaVantage K線 API

    // Spring Boot 會透過 setter / getter 讀寫設定值
    public String getKey() {
        return key;
    }

    // Spring Boot 在啟動時會把 application.yml 的值注入進來
    public void setKey(String key) {
        this.key = key;
    }
}