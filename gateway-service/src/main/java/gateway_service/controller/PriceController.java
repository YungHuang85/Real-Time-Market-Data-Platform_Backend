package gateway_service.controller;

import gateway_service.dto.RawQuoteDTO;
import gateway_service.service.PriceCacheService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")  // 允許所有來源跨域呼叫 API,前端 React 可以直接呼叫這個 API
@RestController  // 宣告這是一個 REST Controller,回傳的物件會自動轉成 JSON
@RequestMapping("/api/price")  // 所有 API 的 base path
public class PriceController {

    private final PriceCacheService cache;  // 負責從 Redis cache 取得即時行情

    public PriceController(PriceCacheService cache) {
        this.cache = cache;  // Spring 會自動注入 PriceCacheService
    }

    // GET /api/price/{symbol}
    // 例如：
    // /api/price/NVDA
    @GetMapping("/{symbol}")
    public RawQuoteDTO get(@PathVariable String symbol) {
    	// 從 Redis 取得該股票的最新價格
        // 回傳 JSON 給前端
        return cache.get(symbol);
    }

    @GetMapping  // 取得所有股票的即時價格
    public Map<String, RawQuoteDTO> all() {
    	// 從 Redis 取得全部價格
        // 回傳 Map<symbol, RawQuoteDTO>
        return cache.getAll();
    }
}