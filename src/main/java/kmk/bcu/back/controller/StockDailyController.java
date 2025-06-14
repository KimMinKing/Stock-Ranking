// StockDailyController.java
package kmk.bcu.back.controller;

import kmk.bcu.back.service.StockDailyService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stockdaily")
public class StockDailyController {

    private final StockDailyService stockDailyService;

    public StockDailyController(StockDailyService stockDailyService) {
        this.stockDailyService = stockDailyService;
    }

    /**
     * 일자별 주가 데이터 조회
     * @param stockCode 종목코드 (예: 005930)
     * @param period 기간 분류 (D: 일별, W: 주별, M: 월별) - 기본값: D
     * @param marketDivCode 시장 분류 (J: KRX, NX: NXT, UN: 통합) - 기본값: J
     * @param adjPrice 수정주가 반영 (0: 미반영, 1: 반영) - 기본값: 1
     * @param custType 고객 타입 (P: 개인, B: 법인) - 기본값: P
     * @return 일자별 주가 데이터
     */
    @GetMapping("/{stockCode}")
    public Mono<String> getStockDailyPrice(@PathVariable String stockCode,
                                           @RequestParam(defaultValue = "D") String period,
                                           @RequestParam(defaultValue = "J") String marketDivCode,
                                           @RequestParam(defaultValue = "1") String adjPrice,
                                           @RequestParam(defaultValue = "P") String custType) {
        return stockDailyService.getStockDailyPrice(marketDivCode, stockCode, period, adjPrice, custType);
    }

    /**
     * 일별 주가 데이터 조회 (기본 설정)
     * @param stockCode 종목코드
     * @return 일별 주가 데이터
     */
    @GetMapping("/{stockCode}/daily")
    public Mono<String> getDailyPrice(@PathVariable String stockCode) {
        return stockDailyService.getStockDailyPrice("J", stockCode, "D", "1", "P");
    }

    /**
     * 주별 주가 데이터 조회
     * @param stockCode 종목코드
     * @return 주별 주가 데이터
     */
    @GetMapping("/{stockCode}/weekly")
    public Mono<String> getWeeklyPrice(@PathVariable String stockCode) {
        return stockDailyService.getStockDailyPrice("J", stockCode, "W", "1", "P");
    }

    /**
     * 월별 주가 데이터 조회
     * @param stockCode 종목코드
     * @return 월별 주가 데이터
     */
    @GetMapping("/{stockCode}/monthly")
    public Mono<String> getMonthlyPrice(@PathVariable String stockCode) {
        return stockDailyService.getStockDailyPrice("J", stockCode, "M", "1", "P");
    }
}