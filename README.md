# Real-Time Market Data Platform (Backend)

Real-time stock and cryptocurrency market data platform built with Spring Boot microservices (backend) and React (frontend).

The system uses Apache Kafka for event streaming, Redis for real-time caching, and WebSocket for live data delivery to the frontend dashboard.

---

# Microservices Architecture

## quote-service

### Responsibilities
- Receive real-time price streams
- Publish market price events to Kafka
- Provide REST APIs for market data

### APIs
- Company profile
- Financial metrics
- Analyst recommendations
- Market news

### Data Sources
- Finnhub API
- Binance API

---

## candle-service

### Responsibilities
- Provide historical candlestick (K-line) data
- Serve chart data for frontend visualization

### Data Source
- AlphaVantage API

---

## gateway-service

### Responsibilities
- Central API Gateway
- Route requests to microservices
- Provide REST and WebSocket endpoints
- Unified API entry point for frontend

---

                    [Alpha Vantage]                 [Binance]                 [Finnhub]
                          |                            |                         |
                          |                            |                         |
                          v                            v                         v
                  +----------------+          +----------------+        +----------------+
                  | candle-service |          | candle-service |        | quote-service  |
                  |  (AlphaVantage |          |  (Binance K線) |        |  (公司/財報/    |
                  |   日 K 線)     |          |                |        |   新聞/即時價)  |
                  +----------------+          +----------------+        +----------------+
                          |                            |                         |
                          | REST: /api/candles/{symbol}|                         |
                          | (回傳 K 線資料)             |                         |
                          |                            |                         |
                          +-------------+--------------+-------------------------+
                                        |
                                        |   (未來可由 front-end 視需要呼叫)
                                        v
           [Kafka: stock.raw topic]  <----  (這部分通常由另一個 quote producer 或外部 feed 寫入)
                      |
                      v
             +------------------+           +----------------------+         +----------------------+
             |  QuoteConsumer   |  ----->   |  PriceCacheService   |  ---->  |      Redis           |
             | (gateway-service)|  Kafka    | (gateway-service)    |  set    |  key: price:SYMBOL   |
             +------------------+  consume  +----------------------+         +----------------------+
                      |
                      | WebSocket 推播 (/topic/price, /topic/price/{symbol})
                      v
             +----------------------+
             |  WebSocketConfig     |
             |  + SimpMessagingTemp.|
             +----------------------+
                      |
                      |                     HTTP
                      |      +-------------------------------------+
                      |      |         REST API (gateway)          |
                      |      |  GET /api/price/{symbol}            |
                      |      |  GET /api/price                     |
                      v      +-------------------------------------+
             +----------------------+                      +---------------------+
             |    gateway-service   |  <-----------------> |   Frontend (React)  |
             |  (API + WebSocket    |    CORS: http://     |  - 呼叫 /api/price  |
             |   對外出口)           |    localhost:5173    |  - 連線 /ws + 訂閱  |
             +----------------------+                      |    /topic/price...  |
                                                           +---------------------+


# Technology Stack

## Backend

- Java
- Spring Boot
- Spring WebSocket
- Spring Kafka

## Infrastructure

- Apache Kafka
- Redis
- Docker

## Architecture

- Microservices Architecture
- Event-Driven Architecture

---

# System Flow

1. Market data is received from external APIs  
2. `quote-service` publishes events to Kafka  
3. Redis caches the latest market data  
4. `gateway-service` pushes updates via WebSocket  
5. React dashboard displays real-time market data  

---

# Features

- Real-time stock and crypto price streaming
- Interactive candlestick charts
- Company profile and financial metrics
- Analyst recommendation aggregation
- Market news integration
- Event-driven microservice architecture

## Architecture Highlights

- Microservices architecture
- Event-driven system design
- Real-time market data streaming
- Kafka-based event pipeline
- WebSocket live dashboard updates
- Redis real-time cache layer

```text
real-time-market-data-platform
│
├─ quote-service                     # Real-time market data ingestion service
│  │                                 # Connects to external market APIs and produces streaming events
│  │
│  ├─ config                         # Configuration classes
│  │   └─ FinnhubProperties.java     # Finnhub API configuration (API key, endpoint)
│  │
│  ├─ controller                     # REST API endpoints
│  │   ├─ CompanyController.java     # API for company profile information
│  │   ├─ MetricController.java      # API for financial metrics (PE, EPS, etc.)
│  │   ├─ NewsController.java        # API for market news
│  │   ├─ QuoteController.java       # API for latest market quotes
│  │   └─ RecommendationController.java # API for analyst recommendations
│  │
│  ├─ dto                            # Data Transfer Objects for API responses
│  │   ├─ CompanyProfileDTO.java
│  │   ├─ MetricDTO.java
│  │   ├─ NewsDTO.java
│  │   ├─ RawQuoteDTO.java
│  │   └─ RecommendationDTO.java
│  │
│  ├─ service                        # Business logic layer
│  │   ├─ FinnhubRestService.java    # Calls Finnhub REST API
│  │   └─ QuoteProducer.java         # Publishes real-time quote events to Kafka
│  │
│  └─ websocket
│      └─ FinnhubWebSocketClient.java # Receives real-time quote stream from Finnhub WebSocket
│
│
├─ candle-service                    # Historical market data service
│  │                                 # Provides candlestick (OHLC) data for charting
│  │
│  ├─ config
│  │   ├─ AlphaVantageProperties.java # AlphaVantage API configuration
│  │   └─ CorsConfig.java             # CORS configuration for frontend access
│  │
│  ├─ controller
│  │   └─ CandleController.java       # REST API to retrieve candlestick data
│  │
│  ├─ dto
│  │   └─ CandleBar.java              # OHLC candlestick data model
│  │
│  └─ service
│      └─ AlphaVantageService.java    # Calls AlphaVantage API to fetch candle data
│
│
├─ gateway-service                   # API Gateway service
│  │                                 # Central entry point for frontend requests
│  │                                 # Handles WebSocket streaming and Redis caching
│  │
│  ├─ config
│  │   ├─ CorsConfig.java             # Cross-Origin configuration
│  │   ├─ RedisConfig.java            # Redis cache configuration
│  │   └─ WebSocketConfig.java        # WebSocket endpoint configuration
│  │
│  ├─ controller
│  │   └─ PriceController.java        # REST API for retrieving cached price data
│  │
│  ├─ dto
│  │   └─ RawQuoteDTO.java            # Quote data format used across services
│  │
│  └─ service
│      ├─ PriceCacheService.java      # Reads/writes real-time prices to Redis cache
│      └─ QuoteConsumer.java          # Kafka consumer receiving market price events
│
│
├─ docker-compose.yml                # Infrastructure services (Kafka, Redis)
│
└─ infrastructure
   ├─ Kafka                          # Event streaming platform for real-time data
   └─ Redis                          # In-memory cache for fast market data access
```

