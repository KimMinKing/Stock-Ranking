
// StockDailyService.java
package kmk.bcu.back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class StockDailyService {

    private final WebClient webClient;

    @Value("${koreainvestment.appkey}")
    private String appKey;

    @Value("${koreainvestment.appsecret}")
    private String appSecret;

    @Value("${koreainvestment.accesstoken}")
    private String accessToken;

    private static final String BASE_URL = "https://openapi.koreainvestment.com:9443";

    public StockDailyService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    /**
     * 일자별 주가 데이터 조회
     * @param marketDivCode 시장 분류 코드
     * @param stockCode 종목코드
     * @param periodDivCode 기간 분류 코드 (D/W/M)
     * @param orgAdjPrice 수정주가 반영 여부
     * @param custType 고객 타입
     * @return 주가 데이터
     */
    public Mono<String> getStockDailyPrice(String marketDivCode, String stockCode,
                                           String periodDivCode, String orgAdjPrice, String custType) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/inquire-daily-price")
                        .queryParam("FID_COND_MRKT_DIV_CODE", marketDivCode)
                        .queryParam("FID_INPUT_ISCD", stockCode)
                        .queryParam("FID_PERIOD_DIV_CODE", periodDivCode)
                        .queryParam("FID_ORG_ADJ_PRC", orgAdjPrice)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", "FHKST01010400")  // 일자별 조회용 TR_ID
                .header("custtype", custType)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> {
                    // 에러 로깅
                    System.err.println("한국투자증권 API 호출 중 오류 발생: " + error.getMessage());
                });
    }

    /**
     * 연속 조회가 필요한 경우 (다음 데이터 조회)
     * @param marketDivCode 시장 분류 코드
     * @param stockCode 종목코드
     * @param periodDivCode 기간 분류 코드
     * @param orgAdjPrice 수정주가 반영 여부
     * @param custType 고객 타입
     * @param trCont 연속 거래 여부 (N: 다음 데이터)
     * @return 주가 데이터
     */
    public Mono<String> getStockDailyPriceWithContinuation(String marketDivCode, String stockCode,
                                                           String periodDivCode, String orgAdjPrice,
                                                           String custType, String trCont) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/inquire-daily-price")
                        .queryParam("FID_COND_MRKT_DIV_CODE", marketDivCode)
                        .queryParam("FID_INPUT_ISCD", stockCode)
                        .queryParam("FID_PERIOD_DIV_CODE", periodDivCode)
                        .queryParam("FID_ORG_ADJ_PRC", orgAdjPrice)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", "FHKST01010400")
                .header("tr_cont", trCont)  // 연속 조회용
                .header("custtype", custType)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> {
                    System.err.println("한국투자증권 API 연속 조회 중 오류 발생: " + error.getMessage());
                });
    }
}
