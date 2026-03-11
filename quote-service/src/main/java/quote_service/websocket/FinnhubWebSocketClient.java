package quote_service.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;
import quote_service.config.FinnhubProperties;
import quote_service.dto.RawQuoteDTO;
import quote_service.service.FinnhubRestService;
import quote_service.service.QuoteProducer;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.*;

@Component  // 讓 Spring 啟動時建立此 WebSocket client
public class FinnhubWebSocketClient {

    private final QuoteProducer producer;  // Kafka producer，用來送行情資料
    private final ObjectMapper mapper;  // JSON 解析器
    private final FinnhubProperties properties;  // API token
    private final FinnhubRestService rest;  // REST service
    private final Set<String> subscribedSymbols = ConcurrentHashMap.newKeySet();  // 記錄目前訂閱的股票

    // 重連排程器
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "finnhub-reconnect");
        t.setDaemon(true);
        return t;
    });

    private volatile WebSocketClient client;  // WebSocket client instance

    public FinnhubWebSocketClient(
            QuoteProducer producer,
            ObjectMapper mapper,
            FinnhubProperties properties,
            FinnhubRestService rest
    ) {
        this.producer = producer;
        this.mapper = mapper;
        this.properties = properties;
        this.rest = rest;
    }

    // Bean 初始化後自動執行
    @PostConstruct
    public void init() {
        connect();  // 啟動 WebSocket 連線
        // 要預設訂閱也可以放這裡（可選）
      
    }

    
    // 對外 API：訂閱 / 取消訂閱
    public void subscribe(String symbol) {

        String s = normalize(symbol);  // symbol 標準化
        if (s.isBlank()) return;

        // 如果是新 symbol 才訂閱
        if (subscribedSymbols.add(s)) {

            sendSubscribe(s);  // 送 WebSocket subscribe

            // fallback price
            double last = rest.getLastClose(s);

            if (last > 0) {

                RawQuoteDTO dto =
                        new RawQuoteDTO(
                                s,
                                last,
                                System.currentTimeMillis(),
                                0
                        );

                producer.send(dto);  // 送 Kafka
            }

            System.out.println("[Finnhub] subscribe requested: " + s);
        }
    }

    // 取消訂閱
    public void unsubscribe(String symbol) {
        String s = normalize(symbol);
        if (s.isBlank()) return;

        if (subscribedSymbols.remove(s)) {
            sendUnsubscribe(s);  // 發送 unsubscribe
            System.out.println("[Finnhub] unsubscribe requested: " + s);
        }
    }


    // WebSocket lifecycle
    // 建立 WebSocket 連線
    private void connect() {
        String url = "wss://ws.finnhub.io?token=" + properties.getToken();

        client = new WebSocketClient(URI.create(url)) {
        	
        	// WebSocket 開啟時
            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("[Finnhub] Connected");

                // 若重連成功，重新訂閱所有股票
                subscribedSymbols.forEach(FinnhubWebSocketClient.this::sendSubscribe);
            }

            // 收到訊息
            @Override
            public void onMessage(String message) {
                try {
                    JsonNode root = mapper.readTree(message);

                    // Finnhub 會回 ping
                    if (root.has("type")) {
                        String type = root.get("type").asText("");
                        if ("ping".equalsIgnoreCase(type)) return;
                        
                        // error message
                        if ("error".equalsIgnoreCase(type) || root.has("error")) {
                            System.out.println("[Finnhub] error msg: " + message);
                            return;
                        }
                    }

                    if (!root.has("data")) return;

                    // 解析 trade data
                    for (JsonNode node : root.get("data")) {
                    	String symbol = node.get("s").asText();
                    	double price = node.get("p").asDouble();

                    	// 如果 price=0 使用 REST fallback
                    	if (price <= 0) {
                    	    price = rest.getLastClose(symbol);
                    	}

                    	RawQuoteDTO dto = new RawQuoteDTO(
                    	        symbol,
                    	        price,
                    	        node.get("t").asLong(),
                    	        node.get("v").asDouble()
                    	);

                    	// 過濾不合法資料
                        if (dto.price() > 0 && dto.symbol() != null && !dto.symbol().isBlank()) {
                            producer.send(dto);  // 發送 Kafka
                        }
                    }

                } catch (Exception e) {
                    System.out.println("[Finnhub] parse error: " + e.getMessage());
                }
            }

            // WebSocket 關閉
            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("[Finnhub] Disconnected (" + code + "): " + reason);
                scheduleReconnect();  // 排程重連
            }

            // WebSocket error
            @Override
            public void onError(Exception ex) {
                System.out.println("[Finnhub] ws error: " + ex.getMessage());
                // onError 後通常也會 close，但保險起見也排重連
                scheduleReconnect();  // 排程重連
            }
        };

        try {
            client.connectBlocking(10, TimeUnit.SECONDS);  // 阻塞連線 10 秒
        } catch (Exception e) {
            System.out.println("[Finnhub] connect failed: " + e.getMessage());
            scheduleReconnect();  // 失敗就重連
        }
    }

    // 排程重連
    private void scheduleReconnect() {
        scheduler.schedule(this::connect, 5, TimeUnit.SECONDS);  // 5 秒後重連
    }

    // =============================
    // Send subscribe/unsubscribe
    // =============================
    
    // 發送 subscribe
    private void sendSubscribe(String symbol) {
        WebSocketClient c = this.client;
        if (!isOpen(c)) return;

        String payload = """
            {"type":"subscribe","symbol":"%s"}
        """.formatted(symbol);

        c.send(payload);
        System.out.println("[Finnhub] subscribed: " + symbol);
    }
    
    // 發送 unsubscribe
    private void sendUnsubscribe(String symbol) {
        WebSocketClient c = this.client;
        if (!isOpen(c)) return;

        String payload = """
            {"type":"unsubscribe","symbol":"%s"}
        """.formatted(symbol);

        c.send(payload);
        System.out.println("[Finnhub] unsubscribed: " + symbol);
    }
    
    // 檢查 WS 是否開啟
    private boolean isOpen(WebSocketClient c) {
        return c != null && c.isOpen();
    }
    
    // symbol 標準化
    private String normalize(String symbol) {
        return symbol == null ? "" : symbol.trim().toUpperCase();
    }
}