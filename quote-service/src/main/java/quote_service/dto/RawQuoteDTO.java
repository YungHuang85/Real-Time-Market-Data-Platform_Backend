package quote_service.dto;

//使用 Java record 定義不可變資料物件，用來封裝即時報價資料
public record RawQuoteDTO(
        String symbol,  // 股票或資產代號
        double price,  // 即時成交價格
        long timestamp,  // 報價時間
        double volume  // 成交量 (交易量)
) {}