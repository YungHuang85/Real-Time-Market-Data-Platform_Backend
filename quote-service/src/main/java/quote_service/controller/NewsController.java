package quote_service.controller;

import org.springframework.web.bind.annotation.*;
import quote_service.dto.NewsDTO;
import quote_service.service.FinnhubRestService;

import java.util.List;

@CrossOrigin("*")  // 允許所有來源的跨域請求 (CORS)，讓前端 React / Vue 可以呼叫此 API
@RestController  // 宣告此類別為 REST Controller，回傳物件會自動轉為 JSON
@RequestMapping("/api/news")  // 設定此 Controller 的 API 基本路徑
public class NewsController {  // 定義股票新聞 API 控制器

    private final FinnhubRestService service;  // 宣告 Service，用來取得股票新聞資料

    public NewsController(FinnhubRestService service) {  // 使用建構子注入 (Constructor Injection)
        this.service = service;  // 將 Spring 注入的 service 指派給成員變數
    }

    @GetMapping("/{symbol}")  // 定義 GET API，例如: /api/news/NVDA
    public List<NewsDTO> getNews(@PathVariable String symbol) {  // 從 URL 取得股票代號 symbol

        return service.getCompanyNews(symbol);  // 呼叫 Service 取得該股票相關新聞並回傳

    }
}