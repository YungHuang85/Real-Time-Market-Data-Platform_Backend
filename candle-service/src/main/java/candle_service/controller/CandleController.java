package candle_service.controller;

import candle_service.dto.CandleBar;
import candle_service.service.AlphaVantageService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController  // 宣告為 REST Controller,回傳物件會自動轉成 JSON
@RequestMapping("/api/candles")  // API base path
public class CandleController {

    private final AlphaVantageService alpha;  // 用來取得股票 K線

    public CandleController(AlphaVantageService alpha) {
        this.alpha = alpha;
    }

    // GET /api/candles/{symbol}
    // 例如：/api/candles/NVDA
    @GetMapping("/{symbol}")
    public ResponseEntity<?> candles(@PathVariable String symbol) {

        try {
            String normalized = normalize(symbol);  // 將 symbol 標準化


            // Crypto
            // 如果是 crypto symbol, 例如：BINANCE:BTCUSDT
            if (normalized.startsWith("BINANCE:") || normalized.endsWith("USDT")) {
                return ResponseEntity.ok(getBinanceCandles(normalized));
            }

            // Stock
            // 例如：NVDA
            List<CandleBar> bars = alpha.getDailyCandles(normalized);
            return ResponseEntity.ok(bars);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());  // 發生錯誤回傳空資料
        }
    }

    // symbol 正規化
    private String normalize(String symbol) {
        if (symbol == null) return "";
        symbol = symbol.trim().toUpperCase();  // 去空白並轉大寫

        // 如果像 BTCUSDT，轉成 BINANCE:BTCUSDT
        if (symbol.endsWith("USDT") && !symbol.contains(":")) {
            return "BINANCE:" + symbol;
        }
        return symbol;
    }

    // Binance K線
    // 呼叫 Binance API 取得加密貨幣 Kline 資料
    // 回傳格式會轉換為 time/open/high/low/close
    private List<Map<String, Object>> getBinanceCandles(String symbol) {

        try {
        	// 移除 BINANCE: 前綴
            // 例如 BINANCE:BTCUSDT → BTCUSDT
            String clean = symbol.replace("BINANCE:", "").toUpperCase();

            // Binance Kline API URL
            // symbol = 交易對 (BTCUSDT / ETHUSDT)
            // interval = K線時間週期 (1h = 1小時)
            // limit = 回傳 K線數量 (最多 100 根)
            String url = "https://api.binance.com/api/v3/klines"
                    + "?symbol=" + clean
                    + "&interval=1h"
                    + "&limit=100";

            // Spring HTTP client
            // 用來呼叫外部 REST API
            RestTemplate rest = new RestTemplate();

            // 呼叫 Binance API
            // 回傳資料型別為：
            // List<List<Object>>
            // 因為 Binance Kline API 是陣列巢狀格式
            ResponseEntity<List<List<Object>>> resp =
                    rest.exchange(url, HttpMethod.GET, null,
                            new ParameterizedTypeReference<>() {});
            // 取得 response body
            List<List<Object>> body = resp.getBody();
            // 如果沒有資料則回傳空集合
            if (body == null || body.isEmpty()) return Collections.emptyList();

            // 建立回傳結果
            // size 預先設定可避免 ArrayList resize
            List<Map<String, Object>> result = new ArrayList<>(body.size());
            // 逐筆解析 Binance Kline
            for (List<Object> k : body) {
                Map<String, Object> candle = new HashMap<>();  // 建立一根 K線資料
                candle.put("time", ((Number) k.get(0)).longValue() / 1000); // k[0] = 開盤時間
                candle.put("open", Double.parseDouble(k.get(1).toString()));  // k[1] = 開盤價
                candle.put("high", Double.parseDouble(k.get(2).toString()));  // k[2] = 最高價
                candle.put("low", Double.parseDouble(k.get(3).toString()));  // k[3] = 最低價
                candle.put("close", Double.parseDouble(k.get(4).toString()));  // k[4] = 收盤價
                result.add(candle);  // 將這根 K線加入結果
            }

            return result;  // 回傳 K線列表

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();  // 回傳空集合避免 API crash
        }
    }
}