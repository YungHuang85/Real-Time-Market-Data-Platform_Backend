package gateway_service.dto;

public record RawQuoteDTO(
        String symbol,  // 股票或資產代號
        double price,  // 最新成交價格
        long timestamp,  // 行情時間戳記,代表這筆價格的時間
        double volume  // 成交量
) {}