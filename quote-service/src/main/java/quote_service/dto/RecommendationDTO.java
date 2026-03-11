package quote_service.dto;

//使用 Java record 定義不可變資料物件，用於封裝分析師推薦評級資料
public record RecommendationDTO(

        int buy,  // 建議買入 (Buy) 的分析師數量
        int hold,  // 建議持有 (Hold) 的分析師數量
        int sell,  // 建議賣出 (Sell) 的分析師數量
        String period  // 評級統計期間，例如 "2026-02"

) {}