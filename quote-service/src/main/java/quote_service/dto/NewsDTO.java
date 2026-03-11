package quote_service.dto;

//使用 Java record 定義不可變資料物件，用於封裝股票新聞資料
public record NewsDTO(

        String headline,  // 新聞標題
        String source,  // 新聞來源
        String url,  // 新聞原始連結
        long datetime,  // 新聞發布時間
        String summary  // 新聞摘要內容

) {}
