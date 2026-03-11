package candle_service.dto;

public record CandleBar(
        long time,  // K線時間戳
        double open,  // 開盤價 (Open)
        double high,  // 最高價 (High)
        double low,  // 最低價 (Low)
        double close  // 收盤價 (Close)
) {}