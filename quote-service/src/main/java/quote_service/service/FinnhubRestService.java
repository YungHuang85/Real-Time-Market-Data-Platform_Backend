package quote_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import quote_service.config.FinnhubProperties;
import quote_service.dto.CompanyProfileDTO;
import quote_service.dto.MetricDTO;
import quote_service.dto.NewsDTO;
import quote_service.dto.RecommendationDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service  // 宣告為 Spring Service Bean，Spring 啟動時會自動建立並管理
public class FinnhubRestService {

    private final FinnhubProperties props;  // Finnhub 設定（主要存放 API token）
    private final ObjectMapper mapper;  // JSON 解析器

    private final HttpClient http = HttpClient.newHttpClient();  // 建立 HTTP Client，用於呼叫 Finnhub API

    public FinnhubRestService(FinnhubProperties props, ObjectMapper mapper) {  // 建構子注入
        this.props = props;  // 注入設定類別
        this.mapper = mapper;  // 注入 JSON parser
    }

    /**
     * 共用 HTTP 呼叫方法
     * 將 HTTP request 與 JSON 解析邏輯集中在此
     */
    private JsonNode call(String url) throws Exception {

        HttpRequest req = HttpRequest.newBuilder()  // 建立 HTTP request
                .uri(URI.create(url))  // 設定 API URL
                .GET()  // 設定為 GET 請求
                .build();  // 建立 request

        HttpResponse<String> resp =
                http.send(req, HttpResponse.BodyHandlers.ofString());  // 發送 HTTP request 並取得回應

        return mapper.readTree(resp.body());  // 將 JSON 字串轉換為 JsonNode 結構
    }

    /**
     * 取得公司基本資料
     */
    public CompanyProfileDTO getCompanyProfile(String symbol) {

        try {

            String url =
                    "https://finnhub.io/api/v1/stock/profile2"  // Finnhub 公司資料 API
                            + "?symbol=" + symbol  // 股票代號
                            + "&token=" + props.getToken();  // API Token

            JsonNode json = call(url);  // 呼叫共用 HTTP 方法

            return new CompanyProfileDTO(  // 將 JSON 資料轉換為 DTO

                    json.path("name").asText(),  // 公司名稱
                    json.path("ticker").asText(),  // 股票代號
                    json.path("exchange").asText(),  // 交易所
                    json.path("ipo").asText(),  // IPO 日期
                    json.path("marketCapitalization").asDouble(),  // 公司市值
                    json.path("weburl").asText(),  // 公司官網
                    json.path("logo").asText(),  // 公司 logo
                    json.path("country").asText(),  // 公司國家
                    json.path("currency").asText()  // 交易幣別

            );

        } catch (Exception e) {

            throw new RuntimeException("Finnhub company profile error", e);  // 轉為 RuntimeException

        }
    }

    /**
     * 取得公司新聞
     */
    public List<NewsDTO> getCompanyNews(String symbol) {

        try {

            String url =
                    "https://finnhub.io/api/v1/company-news"  // Finnhub 公司新聞 API
                            + "?symbol=" + symbol  // 股票代號
                            + "&from=2024-01-01"  // 查詢起始日期
                            + "&to=2026-12-31"  // 查詢結束日期
                            + "&token=" + props.getToken();  // API Token

            JsonNode array = call(url);  // 呼叫 Finnhub API

            List<NewsDTO> list = new ArrayList<>();  // 建立新聞清單

            for (JsonNode node : array) {  // 遍歷 JSON 陣列

                list.add(new NewsDTO(  // 建立 DTO 並加入 list

                        node.path("headline").asText(),  // 新聞標題
                        node.path("source").asText(),  // 新聞來源
                        node.path("url").asText(),  // 新聞連結
                        node.path("datetime").asLong(),  // 發布時間
                        node.path("summary").asText()  // 新聞摘要

                ));

            }

            return list;  // 回傳新聞清單

        } catch (Exception e) {

            throw new RuntimeException("Finnhub news error", e);  // 包裝例外

        }
    }

    /**
     * 取得財務指標
     */
    public MetricDTO getMetric(String symbol) {

        try {

            String url =
                    "https://finnhub.io/api/v1/stock/metric"  // Finnhub 財務指標 API
                            + "?symbol=" + symbol  // 股票代號
                            + "&metric=all"  // 取得所有財務指標
                            + "&token=" + props.getToken();  // API Token

            JsonNode root = call(url);  // 呼叫 API
            JsonNode metric = root.path("metric");  // 取得 metric 節點

            return new MetricDTO(  // 將 JSON 轉換為 DTO

                    metric.path("peNormalizedAnnual").asDouble(),  // 本益比
                    metric.path("epsInclExtraItemsTTM").asDouble(),  // EPS
                    metric.path("pbAnnual").asDouble(),  // 股價淨值比
                    metric.path("dividendYieldIndicatedAnnual").asDouble()  // 股息殖利率

            );

        } catch (Exception e) {

            throw new RuntimeException("Finnhub metric error", e);  // 包裝例外

        }
    }

    /**
     * 取得分析師推薦
     */
    public RecommendationDTO getRecommendation(String symbol) {

        try {

            String url =
                    "https://finnhub.io/api/v1/stock/recommendation"  // Finnhub 分析師推薦 API
                            + "?symbol=" + symbol
                            + "&token=" + props.getToken();

            JsonNode array = call(url);  // 呼叫 API

            if (!array.isArray() || array.isEmpty()) {  // 若沒有資料
                return null;
            }

            JsonNode node = array.get(0);  // 取得最新一筆資料

            return new RecommendationDTO(

                    node.path("buy").asInt(),  // Buy 數量
                    node.path("hold").asInt(),  // Hold 數量
                    node.path("sell").asInt(),  // Sell 數量
                    node.path("period").asText()  // 評級期間

            );

        } catch (Exception e) {

            throw new RuntimeException("Finnhub recommendation error", e);

        }
    }

    /**
     * 取得最新價格
     */
    public double getLastClose(String symbol) {

        try {

            String url =
                    "https://finnhub.io/api/v1/quote"  // Finnhub 即時報價 API
                            + "?symbol=" + symbol
                            + "&token=" + props.getToken();

            JsonNode json = call(url);  // 呼叫 API

            double current = json.path("c").asDouble();  // 即時價格
            double prevClose = json.path("pc").asDouble();  // 前一日收盤價

            return current > 0 ? current : prevClose;  // 若有即時價則回傳，否則回傳前收盤價

        } catch (Exception e) {

            throw new RuntimeException("Finnhub quote error", e);

        }
    }
}