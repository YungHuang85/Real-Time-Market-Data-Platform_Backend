package quote_service.controller;

import org.springframework.web.bind.annotation.*;
import quote_service.dto.CompanyProfileDTO;
import quote_service.service.FinnhubRestService;

@CrossOrigin("*")  // 允許所有來源的跨域請求 (CORS)，通常給前端 React / Vue 呼叫 API
@RestController  // 宣告此類別為 REST Controller，回傳資料會自動轉為 JSON
@RequestMapping("/api/company")  // 設定此 Controller 的 API 基本路徑
public class CompanyController {  // 定義公司資訊 API 的控制器

    private final FinnhubRestService service;  // 宣告 Service，負責實際呼叫 Finnhub API

    public CompanyController(FinnhubRestService service) {  // 建構子注入 (Constructor Injection)
        this.service = service;  // 將 Spring 注入的 service 指派給成員變數
    }

    @GetMapping("/{symbol}")  // 定義 GET API，例如: /api/company/NVDA
    public CompanyProfileDTO getCompany(@PathVariable String symbol) {  // 從 URL 取得股票代號 symbol

        return service.getCompanyProfile(symbol);  // 呼叫 service 取得公司資訊並回傳

    }
}
