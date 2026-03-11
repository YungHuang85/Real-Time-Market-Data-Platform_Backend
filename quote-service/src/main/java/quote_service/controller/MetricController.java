package quote_service.controller;

import org.springframework.web.bind.annotation.*;
import quote_service.dto.MetricDTO;
import quote_service.service.FinnhubRestService;

@CrossOrigin("*")  // 允許所有來源的跨域請求 (CORS)，讓前端 React / Vue 可以呼叫此 API
@RestController  // 宣告此類別為 REST Controller，回傳物件會自動轉成 JSON
@RequestMapping("/api/metric")  // 設定此 Controller 的 API 基本路徑
public class MetricController {  // 定義「公司財務指標 API」控制器

    private final FinnhubRestService service;  // 宣告 Service，負責取得股票財務指標資料

    public MetricController(FinnhubRestService service) {  // 建構子注入 (Constructor Injection)
        this.service = service;  // 將 Spring 容器注入的 service 指派給此類別
    }

    @GetMapping("/{symbol}")  // 定義 GET API，例如: /api/metric/NVDA
    public MetricDTO getMetric(@PathVariable String symbol) {  // 從 URL 取得股票代號 symbol

        return service.getMetric(symbol);  // 呼叫 Service 取得該股票的財務指標並回傳

    }
}
