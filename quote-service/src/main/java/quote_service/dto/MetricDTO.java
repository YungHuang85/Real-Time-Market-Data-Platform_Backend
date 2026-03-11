package quote_service.dto;

//使用 Java record 定義不可變資料物件，用於封裝股票財務指標
public record MetricDTO(

        double pe,  // 本益比 (Price-to-Earnings Ratio, P/E)
        double eps,  // 每股盈餘 (Earnings Per Share, EPS)
        double pb,  // 股價淨值比 (Price-to-Book Ratio, P/B)
        double dividendYield  // 股息殖利率 (Dividend Yield)

) {}