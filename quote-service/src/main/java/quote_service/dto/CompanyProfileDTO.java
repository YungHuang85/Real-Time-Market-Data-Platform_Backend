package quote_service.dto;

//使用 Java record 定義不可變資料物件，用於封裝公司基本資料
public record CompanyProfileDTO(

        String name,  // 公司名稱 (例如 NVIDIA Corporation)
        String ticker,  // 股票代號 (例如 NVDA)
        String exchange,  // 股票交易所 (例如 NASDAQ)
        String ipo,  // 公司 IPO 日期 (上市日期)
        double marketCapitalization,  // 公司市值 (Market Cap)
        String weburl,  // 公司官方網站網址
        String logo,  // 公司 Logo 圖片 URL
        String country,  // 公司所在國家
        String currency  // 股票交易幣別 (例如 USD)

) {}