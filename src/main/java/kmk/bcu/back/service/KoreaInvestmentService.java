package kmk.bcu.back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class KoreaInvestmentService {
    private final TokenService tokenService;
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://openapi.koreainvestment.com:9443")
            .defaultHeader("Content-Type", "application/json")
            .build();

    @Value("${koreainvestment.appkey}")
    private String appKey;

    @Value("${koreainvestment.appsecret}")
    private String appSecret;

    private final String accessToken;

    public KoreaInvestmentService(TokenService tokenService) {
        this.tokenService = tokenService;
        accessToken = tokenService.getAccessToken();
    }

    public Mono<String> getStockData(String path, String trId, String fidCondScrDivCode) {
        String accessToken = tokenService.getAccessToken();
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("fid_rsfl_rate2", "")
                        .queryParam("fid_cond_mrkt_div_code", "J")
                        .queryParam("fid_cond_scr_div_code", fidCondScrDivCode)
                        .queryParam("fid_input_iscd", "0000")
                        .queryParam("fid_rank_sort_cls_code", "0")
                        .queryParam("fid_input_cnt_1", "0")
                        .queryParam("fid_prc_cls_code", "0")
                        .queryParam("fid_input_price_1", "")
                        .queryParam("fid_input_price_2", "")
                        .queryParam("fid_vol_cnt", "")
                        .queryParam("fid_trgt_cls_code", "0")
                        .queryParam("fid_trgt_exls_cls_code", "0")
                        .queryParam("fid_div_cls_code", "0")
                        .queryParam("fid_rsfl_rate1", "")
                        .build())
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .retrieve()
                .bodyToMono(String.class);
    }

    //등락률 순위
    public Mono<String> getfluctuation() {
        return getStockData("/uapi/domestic-stock/v1/ranking/fluctuation", "FHPST01700000", "20170");
    }

    //시가총액 상위
    public Mono<String> getmarket_cap() {
        return getStockData("/uapi/domestic-stock/v1/ranking/market-cap", "FHPST01740000", "20174");
    }

    //수익자산지표 순위
    public Mono<String> getprofit_asset_index() {
        String path="/uapi/domestic-stock/v1/ranking/profit-asset-index";
        String trId="FHPST01730000";
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("fid_cond_mrkt_div_code", "J") // 시장구분코드 (주식 J)
                        .queryParam("fid_trgt_cls_code", "0") // 대상구분코드 (0: 전체)
                        .queryParam("fid_cond_scr_div_code", "20173") // 화면분류코드 (20173 고정)
                        .queryParam("fid_input_iscd", "0000") // 종목코드 (0000: 전체)
                        .queryParam("fid_div_cls_code", "0") // 분류구분코드 (0: 전체)
                        .queryParam("fid_input_price_1", "") // 입력가격1 (가격 ~)
                        .queryParam("fid_input_price_2", "") // 입력가격2 (~ 가격)
                        .queryParam("fid_vol_cnt", "") // 거래량수 (거래량 ~)
                        .queryParam("fid_input_option_1", "2023") // 회계연도
                        .queryParam("fid_input_option_2", "3") // 분기 (3: 결산)
                        .queryParam("fid_rank_sort_cls_code", "0") // 순위정렬 (0: 매출이익)
                        .queryParam("fid_blng_cls_code", "0") // 소속구분코드 (0: 전체)
                        .queryParam("fid_trgt_exls_cls_code", "0") // 대상제외구분코드 (0: 전체)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .header("custtype", "P") // P: 개인, B: 법인
                .retrieve()
                .bodyToMono(String.class);
    }

    //체결강도 상위
    public Mono<String> getvolume_power() {
        return getStockData("/uapi/domestic-stock/v1/ranking/volume-power", "FHPST01680000", "20168");
    }

    //관심종목등록 상위
    public Mono<String> gettop_interest_stock() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/ranking/top-interest-stock")
                        .queryParam("fid_input_iscd_2", "000000") // 필수 입력값
                        .queryParam("fid_cond_mrkt_div_code", "J") // 시장 구분 코드
                        .queryParam("fid_cond_scr_div_code", "20180") // 조건 화면 분류 코드
                        .queryParam("fid_input_iscd", "0000") // 입력 종목코드
                        .queryParam("fid_trgt_cls_code", "0") // 대상 구분 코드
                        .queryParam("fid_trgt_exls_cls_code", "0") // 대상 제외 구분 코드
                        .queryParam("fid_input_price_1", "") // 입력 가격1
                        .queryParam("fid_input_price_2", "") // 입력 가격2
                        .queryParam("fid_vol_cnt", "") // 거래량 수
                        .queryParam("fid_div_cls_code", "0") // 분류 구분 코드
                        .queryParam("fid_input_cnt_1", 1) // 입력 수1
                        .build())
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", "FHPST01800000")
                .retrieve()
                .bodyToMono(String.class);
    }

    //우선주 괴리율 상위 prefer-disparate-ratio
    public Mono<String> getprefer_disparate_ratio(){
        return getStockData("/uapi/domestic-stock/v1/ranking/prefer-disparate-ratio", "FHPST01770000","20177");
    }

    // 이격도 순위
    public Mono<String> getdisparity(){
        String path="/uapi/domestic-stock/v1/ranking/disparity";
        String trId = "FHPST01780000";
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("fid_input_price_2", "")           // 입력 가격2 (~ 가격)
                        .queryParam("fid_cond_mrkt_div_code", "J")     // 시장구분코드 (주식 J)
                        .queryParam("fid_cond_scr_div_code", "20178") // 화면분류코드 (20178 고정)
                        .queryParam("fid_div_cls_code", "0")          // 분류구분코드 (0: 전체)
                        .queryParam("fid_rank_sort_cls_code", "0")    // 순위정렬 (0: 이격도상위순)
                        .queryParam("fid_hour_cls_code", "20")        // 시간구분 (20: 이격도20)
                        .queryParam("fid_input_iscd", "0000")         // 종목코드 (0000: 전체)
                        .queryParam("fid_trgt_cls_code", "0")         // 대상구분코드 (0: 전체)
                        .queryParam("fid_trgt_exls_cls_code", "0")    // 대상제외구분코드 (0: 전체)
                        .queryParam("fid_input_price_1", "")          // 입력 가격1 (가격 ~)
                        .queryParam("fid_vol_cnt", "")                // 거래량 수
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .header("custtype", "P")  // P: 개인, B: 법인
                .retrieve()
                .bodyToMono(String.class);
    }
    //시장가치 순위
    public Mono<String> getmarket_value(){
        String path="/uapi/domestic-stock/v1/ranking/market-value";
        String trId="FHPST01790000";


        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("fid_trgt_cls_code", "0")           // 대상구분코드 (0: 전체)
                        .queryParam("fid_cond_mrkt_div_code", "J")      // 시장구분코드 (주식 J)
                        .queryParam("fid_cond_scr_div_code", "20179")  // 화면분류코드 (20179 고정)
                        .queryParam("fid_input_iscd", "0000")           // 종목코드 (0000: 전체)
                        .queryParam("fid_div_cls_code", "0")            // 분류구분코드 (0: 전체)
                        .queryParam("fid_input_price_1", "")            // 입력가격1 (가격 ~)
                        .queryParam("fid_input_price_2", "")            // 입력가격2 (~ 가격)
                        .queryParam("fid_vol_cnt", "")                  // 거래량 수
                        .queryParam("fid_input_option_1", "2023")       // 회계연도 (예: 2023)
                        .queryParam("fid_input_option_2", "3")          // 분기 (0:1/4분기, 1:반기, 2:3/4분기, 3:결산)
                        .queryParam("fid_rank_sort_cls_code", "23")     // 순위정렬 (23:PER, 24:PBR, 25:PCR, 26:PSR, 27:EPS, 28:EVA, 29:EBITDA, 30:EV/EBITDA, 31:EBITDA/금융비율)
                        .queryParam("fid_blng_cls_code", "0")           // 소속구분코드 (0: 전체)
                        .queryParam("fid_trgt_exls_cls_code", "0")      // 대상제외구분코드 (0: 전체)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .header("custtype", "P")  // P: 개인, B: 법인
                .retrieve()
                .bodyToMono(String.class);
    }

    //하락 상위?
    public Mono<String> getexp_trans_updown(){
        String path="/uapi/domestic-stock/v1/ranking/exp-trans-updown";
        String trId="FHPST01820000";

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("fid_rank_sort_cls_code", "0")      // 순위정렬 (0:상승률, 1:상승폭, 2:보합, 3:하락율, 4:하락폭, 5:체결량, 6:거래대금)
                        .queryParam("fid_cond_mrkt_div_code", "J")      // 시장구분코드 (주식 J)
                        .queryParam("fid_cond_scr_div_code", "20182")  // 화면분류코드 (20182 고정)
                        .queryParam("fid_input_iscd", "0000")           // 종목코드 (0000:전체, 0001:거래소, 1001:코스닥, 2001:코스피200, 4001:KRX100)
                        .queryParam("fid_div_cls_code", "0")            // 분류구분코드 (0:전체, 1:보통주, 2:우선주)
                        .queryParam("fid_aply_rang_prc_1", "")          // 적용범위가격1 (가격 ~)
                        .queryParam("fid_vol_cnt", "")                  // 거래량 수 (거래량 ~)
                        .queryParam("fid_pbmn", "")                     // 거래대금 (거래대금 ~ 천원단위)
                        .queryParam("fid_blng_cls_code", "0")           // 소속구분코드 (0: 전체)
                        .queryParam("fid_mkop_cls_code", "0")           // 장운영구분코드 (0:장전예상, 1:장마감예상)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .header("custtype", "P")  // P: 개인, B: 법인
                .retrieve()
                .bodyToMono(String.class);
    }

    //당사매매?
    public Mono<String> gettraded_by_company(){
        String path="/uapi/domestic-stock/v1/ranking/traded-by-company";
        String trId="FHPST01860000";
        // 현재 날짜 기준으로 기간 설정 (예: 최근 30일)
        LocalDate now = LocalDate.now();
        LocalDate monthAgo = now.minusDays(30);
        String dateFrom = monthAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dateTo = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("fid_trgt_exls_cls_code", "0")       // 대상제외구분코드 (0: 전체)
                        .queryParam("fid_cond_mrkt_div_code", "J")       // 시장구분코드 (주식 J)
                        .queryParam("fid_cond_scr_div_code", "20186")   // 화면분류코드 (20186 고정)
                        .queryParam("fid_div_cls_code", "0")             // 분류구분코드 (0: 전체)
                        .queryParam("fid_rank_sort_cls_code", "1")       // 순위정렬 (0:매도상위, 1:매수상위)
                        .queryParam("fid_input_date_1", dateFrom)        // 시작날짜 (기간~)
                        .queryParam("fid_input_date_2", dateTo)          // 종료날짜 (~기간)
                        .queryParam("fid_input_iscd", "0000")            // 종목코드 (0000: 전체)
                        .queryParam("fid_trgt_cls_code", "0")            // 대상구분코드 (0: 전체)
                        .queryParam("fid_aply_rang_vol", "0")            // 적용범위거래량 (0: 전체)
                        .queryParam("fid_aply_rang_prc_2", "")           // 적용범위가격2 (~ 가격)
                        .queryParam("fid_aply_rang_prc_1", "")           // 적용범위가격1 (가격 ~)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .header("custtype", "P")  // P: 개인, B: 법인
                .retrieve()
                .bodyToMono(String.class);
    }


    //신고/신저근접종목 상위
    public Mono<String> getnear_new_highlow(){
        String path="/uapi/domestic-stock/v1/ranking/near-new-highlow";
        String trId="FHPST01870000";
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("fid_aply_rang_vol", "0")            // 적용범위거래량 (0: 전체, 100: 100주이상)
                        .queryParam("fid_cond_mrkt_div_code", "J")       // 시장구분코드 (주식 J)
                        .queryParam("fid_cond_scr_div_code", "20187")   // 화면분류코드 (20187 고정)
                        .queryParam("fid_div_cls_code", "0")             // 분류구분코드 (0: 전체)
                        .queryParam("fid_input_cnt_1", "0")              // 괴리율 최소
                        .queryParam("fid_input_cnt_2", "100")            // 괴리율 최대
                        .queryParam("fid_prc_cls_code", "0")             // 가격구분코드 (0:신고근접, 1:신저근접)
                        .queryParam("fid_input_iscd", "0000")            // 종목코드 (0000: 전체)
                        .queryParam("fid_trgt_cls_code", "0")            // 대상구분코드 (0: 전체)
                        .queryParam("fid_trgt_exls_cls_code", "0")       // 대상제외구분코드 (0: 전체)
                        .queryParam("fid_aply_rang_prc_1", "")           // 적용범위가격1 (가격 ~)
                        .queryParam("fid_aply_rang_prc_2", "")           // 적용범위가격2 (~ 가격)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .header("custtype", "P")  // P: 개인, B: 법인
                .retrieve()
                .bodyToMono(String.class);
    }

    //대량 체결 상위
    public Mono<String> getbulk_trans_num(){
        String path="/uapi/domestic-stock/v1/ranking/bulk-trans-num";
        String trId="FHPST01880000";
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("fid_aply_rang_prc_2", "")           // 적용범위가격2 (~ 가격)
                        .queryParam("fid_cond_mrkt_div_code", "J")       // 시장구분코드 (주식 J)
                        .queryParam("fid_cond_scr_div_code", "11909")   // 화면분류코드 (11909 고정)
                        .queryParam("fid_input_iscd", "0000")            // 종목코드 (0000: 전체)
                        .queryParam("fid_rank_sort_cls_code", "0")       // 순위정렬 (0:매수상위, 1:매도상위)
                        .queryParam("fid_div_cls_code", "0")             // 분류구분코드 (0: 전체)
                        .queryParam("fid_input_price_1", "")             // 입력가격1 (건별금액 ~)
                        .queryParam("fid_aply_rang_prc_1", "")           // 적용범위가격1 (가격 ~)
                        .queryParam("fid_input_iscd_2", "")              // 입력종목코드2 (공백:전체, 개별종목시 종목코드)
                        .queryParam("fid_trgt_exls_cls_code", "0")       // 대상제외구분코드 (0: 전체)
                        .queryParam("fid_trgt_cls_code", "0")            // 대상구분코드 (0: 전체)
                        .queryParam("fid_vol_cnt", "")                   // 거래량수 (거래량 ~)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .header("custtype", "P")  // P: 개인, B: 법인
                .retrieve()
                .bodyToMono(String.class);
    }

    //공매도 상위
    public Mono<String> getshort_sale(){
        String path="/uapi/domestic-stock/v1/ranking/short-sale";
        String trId="FHPST04820000";
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("FID_APLY_RANG_VOL", "") // 적용범위거래량 (공백)
                        .queryParam("FID_COND_MRKT_DIV_CODE", "J") // 시장구분코드 (주식 J)
                        .queryParam("FID_COND_SCR_DIV_CODE", "20482") // 화면분류코드 (20482 고정)
                        .queryParam("FID_INPUT_ISCD", "0000") // 종목코드 (0000: 전체)
                        .queryParam("FID_PERIOD_DIV_CODE", "D") // 조회구분 (D: 일, M: 월)
                        .queryParam("FID_INPUT_CNT_1", "0") // 조회기간 (0: 1일)
                        .queryParam("FID_TRGT_EXLS_CLS_CODE", "") // 대상제외구분코드 (공백)
                        .queryParam("FID_TRGT_CLS_CODE", "") // 대상구분코드 (공백)
                        .queryParam("FID_APLY_RANG_PRC_1", "") // 적용범위가격1 (가격 ~)
                        .queryParam("FID_APLY_RANG_PRC_2", "") // 적용범위가격2 (~ 가격)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .header("custtype", "P") // P: 개인, B: 법인
                .retrieve()
                .bodyToMono(String.class);
    }

    //시간외 거래량
    public Mono<String> getovertime_volume(){
        String path="/uapi/domestic-stock/v1/ranking/overtime-volume";
        String trId="FHPST02350000";
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("FID_COND_MRKT_DIV_CODE", "J") // 시장구분코드 (J: 주식)
                        .queryParam("FID_COND_SCR_DIV_CODE", "20235") // 화면분류코드 (20235 고정)
                        .queryParam("FID_INPUT_ISCD", "0000") // 종목코드 (0000: 전체)
                        .queryParam("FID_RANK_SORT_CLS_CODE", "2") // 순위정렬 (2: 거래량)
                        .queryParam("FID_INPUT_PRICE_1", "") // 입력가격1 (가격 ~)
                        .queryParam("FID_INPUT_PRICE_2", "") // 입력가격2 (~ 가격)
                        .queryParam("FID_VOL_CNT", "") // 거래량수 (거래량 ~)
                        .queryParam("FID_TRGT_CLS_CODE", "") // 대상구분코드 (공백)
                        .queryParam("FID_TRGT_EXLS_CLS_CODE", "") // 대상제외구분코드 (공백)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", trId)
                .header("custtype", "P") // P: 개인, B: 법인
                .retrieve()
                .bodyToMono(String.class);
    }
}
