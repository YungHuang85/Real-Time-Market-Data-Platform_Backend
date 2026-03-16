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
