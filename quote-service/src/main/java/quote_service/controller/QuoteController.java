package quote_service.controller;

import org.springframework.web.bind.annotation.*;
import quote_service.websocket.FinnhubWebSocketClient;

@CrossOrigin("*")  // 允許所有來源跨域請求 (CORS)，讓前端 React / Vue 可以呼叫 API
@RestController  // 宣告此類別為 REST Controller，回傳資料會自動轉為 JSON 或字串
@RequestMapping("/api/quote")  // 設定此 Controller 的 API 基本路徑
public class QuoteController {  // 定義即時行情控制器

    private final FinnhubWebSocketClient finnhub;  // 宣告 Finnhub WebSocket Client，用來管理即時行情訂閱

    public QuoteController(FinnhubWebSocketClient finnhub) {  // 使用建構子注入 (Constructor Injection)
        this.finnhub = finnhub;  // 將 Spring 注入的 WebSocket Client 指派給成員變數
    }

    @PostMapping("/subscribe/{symbol}")  // 定義 POST API，例如: /api/quote/subscribe/NVDA
    public String subscribe(@PathVariable String symbol) {  // 從 URL 取得股票代號 symbol
        finnhub.subscribe(symbol);  // 呼叫 WebSocket client 訂閱該股票即時行情
        return "Subscribed " + symbol;  // 回傳訂閱成功訊息
    }

    @PostMapping("/unsubscribe/{symbol}")  // 定義 POST API，例如: /api/quote/unsubscribe/NVDA
    public String unsubscribe(@PathVariable String symbol) {  // 從 URL 取得股票代號 symbol
        finnhub.unsubscribe(symbol);  // 呼叫 WebSocket client 取消訂閱該股票即時行情
        return "Unsubscribed " + symbol;  // 回傳取消訂閱成功訊息
    }
}