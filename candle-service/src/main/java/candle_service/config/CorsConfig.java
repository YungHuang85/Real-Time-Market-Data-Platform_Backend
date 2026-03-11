package candle_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration  // 宣告為 Spring Configuration class
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProps props;  // 從 application.yml 讀取 CORS 設定

    public CorsConfig(CorsProps props) {
        this.props = props;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 只允許 /api/** 的 API 進行 CORS
        // 例如：/api/candles/NVDA
        registry.addMapping("/api/**")
        		// 從 application.yml 讀取允許的來源, 例如：http://localhost:5173
                .allowedOrigins(props.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods("*")  // 允許所有 HTTP 方法
                .allowedHeaders("*");  // 允許所有 request headers
    }

    
    // 將 application.yml 中 cors 的設定綁定到此 class
    // 例如：
    //
    // cors:
    //   allowed-origins:
    //     - http://localhost:5173
    @Component  // 宣告為 Spring Bean
    @org.springframework.boot.context.properties.ConfigurationProperties(prefix = "cors")
    public static class CorsProps {
        private List<String> allowedOrigins;  // 儲存允許的來源清單
        
        // getter 方法,用來取得 CORS 允許的來源清單 (allowed origins)
        // Spring 在設定 CORS 時會呼叫這個方法
        public List<String> getAllowedOrigins() { return allowedOrigins; }
        
        // setter 方法
        // Spring Boot 在啟動時會將 application.yml 的設定值注入進來
        // 例如：
        // cors:
        //   allowed-origins:
        //     - http://localhost:5173
        // Spring 會把這些值轉成 List<String> 並呼叫這個 setter
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }
}