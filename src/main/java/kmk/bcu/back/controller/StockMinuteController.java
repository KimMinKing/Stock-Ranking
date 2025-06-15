package kmk.bcu.back.controller;

import kmk.bcu.back.service.StockMinuteService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stockminute")
public class StockMinuteController {

    private final StockMinuteService stockMinuteService;

    public StockMinuteController(StockMinuteService stockMinuteService) {
        this.stockMinuteService = stockMinuteService;
    }

    /**
     * 주식일별분봉조회 (기본 - 한 번 호출, 최대 120건)
     * @param stockCode 종목코드 (예: 005930)
     * @param marketDivCode 시장 분류 (J: KRX, NX: NXT, UN: 통합) - 기본값: J
     * @param inputDate 입력 날짜 (YYYYMMDD 형식, 예: 20241108) - 기본값: 오늘 날짜
     * @param inputHour 입력 시간 (HHMMSS 형식, 예: 140000) - 기본값: 153000 (15:30:00)
     * @param pwDataIncuYn 과거 데이터 포함 여부 (Y/N) - 기본값: Y
     * @param fakeTickIncuYn 허봉 포함 여부 - 기본값: 공백
     * @param custType 고객 타입 (P: 개인, B: 법인) - 기본값: P
     * @return 분봉 데이터 (최대 120건)
     */
    @GetMapping("/{stockCode}")
    public Mono<String> getStockMinuteChart(@PathVariable String stockCode,
                                            @RequestParam(defaultValue = "J") String marketDivCode,
                                            @RequestParam(required = false) String inputDate,
                                            @RequestParam(defaultValue = "153000") String inputHour,
                                            @RequestParam(defaultValue = "Y") String pwDataIncuYn,
                                            @RequestParam(defaultValue = "") String fakeTickIncuYn,
                                            @RequestParam(defaultValue = "P") String custType) {
        return stockMinuteService.getStockMinuteChart(
                marketDivCode, stockCode, inputDate, inputHour,
                pwDataIncuYn, fakeTickIncuYn, custType
        );
    }

    /**
     * 하루 전체 분봉 데이터 조회 (연속 조회 사용)
     * @param stockCode 종목코드
     * @param inputDate 입력 날짜 (YYYYMMDD) - 기본값: 오늘 날짜
     * @param marketDivCode 시장 분류 - 기본값: J
     * @param custType 고객 타입 - 기본값: P
     * @return 하루 전체 분봉 데이터 (통합된 JSON)
     */
    @GetMapping("/{stockCode}/full-day")
    public Mono<String> getFullDayMinuteChart(@PathVariable String stockCode,
                                              @RequestParam(required = false) String inputDate,
                                              @RequestParam(defaultValue = "J") String marketDivCode,
                                              @RequestParam(defaultValue = "P") String custType) {
        return stockMinuteService.getFullDayMinuteChart(marketDivCode, stockCode, inputDate, custType);
    }

    /**
     * 특정 날짜 하루 전체 분봉 조회 (간편 메서드)
     * @param stockCode 종목코드
     * @param date 조회할 날짜 (YYYYMMDD)
     * @return 하루 전체 분봉 데이터
     */
    @GetMapping("/{stockCode}/date/{date}/full")
    public Mono<String> getFullDayMinuteChartByDate(@PathVariable String stockCode,
                                                    @PathVariable String date) {
        return stockMinuteService.getFullDayMinuteChart("J", stockCode, date, "P");
    }

    /**
     * 현재 날짜 분봉 조회 (기본 설정, 최대 120건)
     * @param stockCode 종목코드
     * @return 현재 날짜 분봉 데이터
     */
    @GetMapping("/{stockCode}/current")
    public Mono<String> getCurrentMinuteChart(@PathVariable String stockCode) {
        return stockMinuteService.getStockMinuteChart(
                "J", stockCode, null, "153000", "Y", "", "P"
        );
    }
}