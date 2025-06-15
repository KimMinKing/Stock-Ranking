package kmk.bcu.back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class KoreaInvestmentService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://openapi.koreainvestment.com:9443")
            .defaultHeader("Content-Type", "application/json")
            .build();

    @Value("${koreainvestment.appkey}")
    private String appKey;

    @Value("${koreainvestment.appsecret}")
    private String appSecret;

    @Value("${koreainvestment.accesstoken}")
    private String accessToken;

    public Mono<String> getStockData(String path, String trId, String fidCondScrDivCode) {
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
        return getStockData("/uapi/domestic-stock/v1/ranking/profit-asset-index", "FHPST01730000", "20173");
    }

    public Mono<String> getvolume_power() {
        return getStockData("/uapi/domestic-stock/v1/ranking/volume-power", "FHPST01680000", "20168");
    }

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
    
}
