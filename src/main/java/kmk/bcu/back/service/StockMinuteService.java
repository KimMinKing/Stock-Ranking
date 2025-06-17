package kmk.bcu.back.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class StockMinuteService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    @Value("${koreainvestment.appkey}")
    private String appKey;

    @Value("${koreainvestment.appsecret}")
    private String appSecret;


    private static final String BASE_URL = "https://openapi.koreainvestment.com:9443";

    public StockMinuteService(WebClient.Builder webClientBuilder,TokenService tokenService) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.objectMapper = new ObjectMapper();
        this.tokenService = tokenService;
    }

    /**
     * 특정 시간의 주식 분봉 데이터를 조회합니다.
     * @param marketDivCode 시장 구분 코드
     * @param stockCode 주식 코드
     * @param inputDate 조회할 날짜
     * @param inputHour 조회할 시간
     * @param pwDataIncuYn 전일 데이터 포함 여부
     * @param fakeTickIncuYn 가상 체결 포함 여부
     * @param custType 고객 유형
     * @return API 응답을 Mono로 반환
     */
    public Mono<String> getStockMinuteChart(String marketDivCode, String stockCode,
                                            String inputDate, String inputHour,
                                            String pwDataIncuYn, String fakeTickIncuYn,
                                            String custType) {
        String dateToUse = inputDate != null ? inputDate :
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return makeApiCall(marketDivCode, stockCode, dateToUse, inputHour,
                pwDataIncuYn, fakeTickIncuYn, custType, "");
    }

    /**
     * 하루 전체 분봉 데이터를 시간대별로 나누어 조회합니다.
     * @param marketDivCode 시장 구분 코드
     * @param stockCode 주식 코드
     * @param inputDate 조회할 날짜
     * @param custType 고객 유형
     * @return API 응답을 Mono로 반환
     */
    public Mono<String> getFullDayMinuteChart(String marketDivCode, String stockCode,
                                              String inputDate, String custType) {
        String dateToUse = inputDate != null ? inputDate :
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return getFullDayDataByTimeSegments(marketDivCode, stockCode, dateToUse, custType);
    }

    /**
     * 시간대별로 분할하여 분봉 데이터를 조회합니다.
     * @param marketDivCode 시장 구분 코드
     * @param stockCode 주식 코드
     * @param inputDate 조회할 날짜
     * @param custType 고객 유형
     * @return 분봉 데이터 리스트를 Mono로 반환
     */
    private Mono<String> getFullDayDataByTimeSegments(String marketDivCode, String stockCode,
                                                      String inputDate, String custType) {
        // 30분 단위로 세밀하게 분할하여 조회
        String[] timeSegments = {
                "093000", // 09:30 (장 시작)
                "113000", // 11:30
                "133000", // 13:30
                "153000"  // 15:30 (장 마감)
        };

        List<JsonNode> allData = new ArrayList<>();
        Set<String> processedTimes = new HashSet<>(); // 중복 방지용

        return processTimeSegments(marketDivCode, stockCode, inputDate, custType,
                timeSegments, 0, allData, processedTimes)
                .map(this::combineMinuteData);
    }

    /**
     * 시간대별로 순차적으로 API를 호출하여 분봉 데이터를 수집합니다.
     * @param marketDivCode 시장 구분 코드
     * @param stockCode 주식 코드
     * @param inputDate 조회할 날짜
     * @param custType 고객 유형
     * @param timeSegments 시간대 배열
     * @param index 현재 처리 중인 시간대 인덱스
     * @param allData 수집된 모든 데이터
     * @param processedTimes 이미 처리된 시간대
     * @return 분봉 데이터 리스트를 Mono로 반환
     */
    private Mono<List<JsonNode>> processTimeSegments(String marketDivCode, String stockCode,
                                                     String inputDate, String custType,
                                                     String[] timeSegments, int index,
                                                     List<JsonNode> allData, Set<String> processedTimes) {
        if (index >= timeSegments.length) {
            return Mono.just(allData);
        }

        String currentTime = timeSegments[index];

        return makeApiCall(marketDivCode, stockCode, inputDate, currentTime,
                "Y", "", custType, "")
                .flatMap(response -> {
                    try {
                        JsonNode jsonResponse = objectMapper.readTree(response);

                        // 응답 상태 확인
                        String rtCd = jsonResponse.path("rt_cd").asText("");
                        if (!"0".equals(rtCd)) {
                            // 오류가 있어도 다음 시간대 계속 처리
                            return processTimeSegments(marketDivCode, stockCode, inputDate, custType,
                                    timeSegments, index + 1, allData, processedTimes);
                        }

                        JsonNode output2 = jsonResponse.get("output2");

                        if (output2 != null && output2.isArray()) {
                            for (JsonNode item : output2) {
                                String itemTime = item.path("stck_cntg_hour").asText("");
                                String itemDate = item.path("stck_bsop_date").asText("");
                                String uniqueKey = itemDate + "_" + itemTime;

                                // 중복 제거
                                if (!processedTimes.contains(uniqueKey)) {
                                    processedTimes.add(uniqueKey);
                                    allData.add(item);
                                }
                            }
                        }

                        // 다음 시간대 처리
                        return Mono.delay(java.time.Duration.ofSeconds(0))
                                .then(processTimeSegments(marketDivCode, stockCode, inputDate, custType,
                                        timeSegments, index + 1, allData, processedTimes));

                    } catch (Exception e) {
                        // 오류가 있어도 다음 시간대 계속 처리
                        return processTimeSegments(marketDivCode, stockCode, inputDate, custType,
                                timeSegments, index + 1, allData, processedTimes);
                    }
                })
                .onErrorResume(error -> {
                    return processTimeSegments(marketDivCode, stockCode, inputDate, custType,
                            timeSegments, index + 1, allData, processedTimes);
                });
    }

    /**
     * 개선된 연속 조회 방식으로 하루 전체 분봉 데이터를 조회합니다.
     * @param marketDivCode 시장 구분 코드
     * @param stockCode 주식 코드
     * @param inputDate 조회할 날짜
     * @param custType 고객 유형
     * @return 분봉 데이터 리스트를 Mono로 반환
     */
    public Mono<String> getFullDayMinuteChartWithContinuation(String marketDivCode, String stockCode,
                                                              String inputDate, String custType) {
        String dateToUse = inputDate != null ? inputDate :
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return getFullDayDataWithImprovedContinuation(marketDivCode, stockCode, dateToUse, custType, "", new ArrayList<>(), new HashSet<>(), 1)
                .map(this::combineMinuteData);
    }

    /**
     * 개선된 연속 조회 로직으로 분봉 데이터를 조회합니다.
     * @param marketDivCode 시장 구분 코드
     * @param stockCode 주식 코드
     * @param inputDate 조회할 날짜
     * @param custType 고객 유형
     * @param trCont 연속 조회 여부
     * @param allData 수집된 모든 데이터
     * @param processedTimes 이미 처리된 시간대
     * @param callCount 호출 횟수
     * @return 분봉 데이터 리스트를 Mono로 반환
     */
    private Mono<List<JsonNode>> getFullDayDataWithImprovedContinuation(String marketDivCode, String stockCode,
                                                                        String inputDate, String custType,
                                                                        String trCont, List<JsonNode> allData,
                                                                        Set<String> processedTimes, int callCount) {
        return makeApiCall(marketDivCode, stockCode, inputDate, "153000",
                "Y", "", custType, trCont)
                .flatMap(response -> {
                    try {
                        JsonNode jsonResponse = objectMapper.readTree(response);

                        // 응답 상태 확인
                        String rtCd = jsonResponse.path("rt_cd").asText("");
                        if (!"0".equals(rtCd)) {
                            return Mono.just(allData);
                        }

                        // 응답 헤더에서 tr_cont 값 확인
                        String responseTrCont = "";
                        if (jsonResponse.has("tr_cont")) {
                            responseTrCont = jsonResponse.get("tr_cont").asText("");
                        }

                        // output2 배열 데이터 처리
                        JsonNode output2 = jsonResponse.get("output2");

                        if (output2 != null && output2.isArray()) {
                            for (JsonNode item : output2) {
                                String itemTime = item.path("stck_cntg_hour").asText("");
                                String itemDate = item.path("stck_bsop_date").asText("");
                                String uniqueKey = itemDate + "_" + itemTime;

                                // 중복 제거
                                if (!processedTimes.contains(uniqueKey)) {
                                    processedTimes.add(uniqueKey);
                                    allData.add(item);
                                }
                            }
                        }

                        // 연속 조회 조건 확인
                        boolean shouldContinue = false;
                        String nextTrCont = "";

                        if ("M".equals(responseTrCont)) {
                            shouldContinue = true;
                            nextTrCont = "N";
                        } else if (callCount < 5) {
                            shouldContinue = true;
                            nextTrCont = "N";
                        }

                        if (shouldContinue) {
                            return Mono.delay(java.time.Duration.ofSeconds(1))
                                    .then(getFullDayDataWithImprovedContinuation(marketDivCode, stockCode, inputDate,
                                            custType, nextTrCont, allData, processedTimes, callCount + 1));
                        } else {
                            return Mono.just(allData);
                        }

                    } catch (Exception e) {
                        return Mono.just(allData);
                    }
                })
                .onErrorResume(error -> {
                    return Mono.just(allData);
                });
    }

    /**
     * 수집된 분봉 데이터를 하나의 JSON으로 통합합니다.
     * @param allData 수집된 모든 분봉 데이터
     * @return 통합된 JSON 문자열
     */
    private String combineMinuteData(List<JsonNode> allData) {
        try {
            ObjectNode result = objectMapper.createObjectNode();
            result.put("rt_cd", "0");
            result.put("msg_cd", "MCA00000");
            result.put("msg1", "정상처리 되었습니다.");
            result.put("total_count", allData.size());

            ArrayNode dataArray = objectMapper.createArrayNode();
            // 시간 순으로 정렬 (가장 오래된 시간부터)
            allData.stream()
                    .sorted((a, b) -> {
                        String timeA = a.path("stck_cntg_hour").asText("");
                        String timeB = b.path("stck_cntg_hour").asText("");
                        return timeA.compareTo(timeB);
                    })
                    .forEach(dataArray::add);

            result.set("minute_data", dataArray);
            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            return "{\"error\":\"데이터 통합 실패\"}";
        }
    }

    /**
     * 실제 API 호출을 수행하는 메서드
     * @param marketDivCode 시장 구분 코드
     * @param stockCode 주식 코드
     * @param inputDate 조회할 날짜
     * @param inputHour 조회할 시간
     * @param pwDataIncuYn 전일 데이터 포함 여부
     * @param fakeTickIncuYn 가상 체결 포함 여부
     * @param custType 고객 유형
     * @param trCont 연속 조회 여부
     * @return API 응답을 Mono로 반환
     */
    private Mono<String> makeApiCall(String marketDivCode, String stockCode,
                                     String inputDate, String inputHour,
                                     String pwDataIncuYn, String fakeTickIncuYn,
                                     String custType, String trCont) {
        String accessToken = tokenService.getAccessToken();
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice")
                        .queryParam("FID_COND_MRKT_DIV_CODE", marketDivCode)
                        .queryParam("FID_INPUT_ISCD", stockCode)
                        .queryParam("FID_INPUT_DATE_1", inputDate)
                        .queryParam("FID_INPUT_HOUR_1", inputHour)
                        .queryParam("FID_PW_DATA_INCU_YN", pwDataIncuYn)
                        .queryParam("FID_FAKE_TICK_INCU_YN", fakeTickIncuYn)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", "FHKST03010230")
                .header("tr_cont", trCont)  // 연속 조회용
                .header("custtype", custType)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> {
                })
                .doOnNext(response -> {
                });
    }
}
