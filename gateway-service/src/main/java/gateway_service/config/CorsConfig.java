package gateway_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration  // 表示此類別為 Spring 設定類別, Spring Boot 啟動時會載入這個 config
public class CorsConfig {

    @Bean  // 將 corsConfigurer 註冊為 Spring Bean
    public WebMvcConfigurer corsConfigurer() {
    	
    	// 回傳 WebMvcConfigurer 物件
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

            	// 設定所有 API 路徑都允許 CORS
            	// /** 代表所有 URL
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")  // 允許來自這個來源的請求
                        .allowedMethods("*")  // 允許所有 HTTP 方法
                        .allowedHeaders("*");  // 允許所有 HTTP headers

            }
        };
    }
}