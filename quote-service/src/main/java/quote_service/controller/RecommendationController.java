package quote_service.controller;

import org.springframework.web.bind.annotation.*;
import quote_service.dto.RecommendationDTO;
import quote_service.service.FinnhubRestService;

@CrossOrigin("*")  // 允許所有來源的跨域請求 (CORS)，讓前端 React / Vue 可以呼叫此 API
@RestController  // 宣告此類別為 REST Controller，回傳物件會自動轉為 JSON
@RequestMapping("/api/recommendation")  // 設定此 Controller 的 API 基本路徑
public class RecommendationController {  // 定義分析師推薦評級 API 控制器

    private final FinnhubRestService service;  // 宣告 Service，用於取得股票分析師推薦資料

    public RecommendationController(FinnhubRestService service) {  // 使用建構子注入 (Constructor Injection)
        this.service = service;  // 將 Spring 容器注入的 service 指派給此類別
    }

    @GetMapping("/{symbol}")  // 定義 GET API，例如: /api/recommendation/NVDA
    public RecommendationDTO getRecommendation(@PathVariable String symbol) {  // 從 URL 取得股票代號 symbol

        return service.getRecommendation(symbol);  // 呼叫 Service 取得分析師推薦資料並回傳

    }
}