package quote_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//將此類別註冊為 Spring 管理的元件，啟動時會自動建立 Bean
@Component
@ConfigurationProperties(prefix = "finnhub")  // 將設定檔中 finnhub.* 開頭的屬性自動映射到此類別
public class FinnhubProperties {  // 定義設定類別，用來存放 Finnhub API 相關設定

    private String token;  // 儲存 Finnhub API Token，對應設定檔中的 finnhub.token

    // 提供 getter 方法，讓其他類別可以讀取 token
    public String getToken() {
    	// 回傳目前儲存的 token 值
        return token;
    }

    // 提供 setter 方法，讓 Spring 在啟動時注入設定值
    public void setToken(String token) {
        this.token = token;  // 將設定檔中的 token 值存入此物件
    }
}