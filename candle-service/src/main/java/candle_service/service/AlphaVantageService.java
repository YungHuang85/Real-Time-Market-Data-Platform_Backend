package candle_service.service;

import candle_service.config.AlphaVantageProperties;
import candle_service.dto.CandleBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service  // 宣告為 Spring Service Bean
public class AlphaVantageService {

    private final AlphaVantageProperties props;  // 取得 AlphaVantage API key
    private final ObjectMapper mapper;  // JSON 解析器
    private final HttpClient http = HttpClient.newHttpClient();  // Java HTTP client，用來呼叫外部 REST API

    public AlphaVantageService(AlphaVantageProperties props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
    }

    // 取得指定股票的日 K線
    public List<CandleBar> getDailyCandles(String symbol) throws Exception {

    	// AlphaVantage API URL
        // function = TIME_SERIES_DAILY (日K)
        // symbol = 股票代號 (NVDA / AAPL)
        // outputsize = compact (最近100筆)
        // apikey = API key
        String url = "https://www.alphavantage.co/query"
                + "?function=TIME_SERIES_DAILY"
                + "&symbol=" + symbol
                + "&outputsize=compact"
                + "&apikey=" + props.getKey();

        // 建立 HTTP GET request
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());  // 呼叫 API 並取得 response
        JsonNode root = mapper.readTree(resp.body());  // 解析 JSON response

        // AlphaVantage 有幾種錯誤回傳:Error Message → symbol錯誤
        if (root.has("Error Message")) {
            throw new IllegalArgumentException("AlphaVantage Error: " + root.get("Error Message").asText());
        }
        // Note → API rate limit
        if (root.has("Note")) {
            throw new IllegalStateException("AlphaVantage Rate Limit: " + root.get("Note").asText());
        }
        // 取得日K線資料節點
        JsonNode series = root.get("Time Series (Daily)");
        if (series == null || series.isNull()) {

            System.out.println("AlphaVantage response: " + root);  // 若 API 回傳格式異常，印出完整 JSON

            return Collections.emptyList();
        }

        List<CandleBar> candles = new ArrayList<>();  // 建立 K線集合
        // AlphaVantage JSON 結構是
        // "2024-01-01" : { ... }
        // 所以用 fieldNames() 取得所有日期
        Iterator<String> it = series.fieldNames();

        while (it.hasNext()) {
            String dateStr = it.next(); // 取得日期字串,格式 yyyy-MM-dd
            JsonNode day = series.get(dateStr);  // 取得當天 K線資料
            
            // 將日期轉為 Unix timestamp
            long epoch = LocalDate.parse(dateStr)
                    .atStartOfDay()
                    .toEpochSecond(ZoneOffset.UTC);

            // 建立 CandleBar DTO
            // AlphaVantage JSON key：
            // 1. open
            // 2. high
            // 3. low
            // 4. close
            candles.add(new CandleBar(
                    epoch,
                    day.get("1. open").asDouble(),
                    day.get("2. high").asDouble(),
                    day.get("3. low").asDouble(),
                    day.get("4. close").asDouble()
            ));
        }

        // 依時間排序
        candles.sort(Comparator.comparingLong(CandleBar::time));
        return candles;  // 回傳 K線資料
    }
}