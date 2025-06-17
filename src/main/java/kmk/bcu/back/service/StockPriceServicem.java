package kmk.bcu.back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class StockPriceServicem {

    private final WebClient webClient;
    private final TokenService tokenService;

    @Value("${koreainvestment.appkey}")
    private String appKey;

    @Value("${koreainvestment.appsecret}")
    private String appSecret;



    private static final String BASE_URL = "https://openapi.koreainvestment.com:9443";

    public StockPriceServicem(WebClient.Builder webClientBuilder, TokenService tokenService) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.tokenService = tokenService;
    }

    public Mono<String> getStockPrice(String marketDivCode, String stockCode, String custType) {
        String accessToken = tokenService.getAccessToken();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/inquire-price")
                        .queryParam("FID_COND_MRKT_DIV_CODE", marketDivCode)
                        .queryParam("FID_INPUT_ISCD", stockCode)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", "FHKST01010100")
                .header("custtype", custType)
                .retrieve()
                .bodyToMono(String.class);
    }
}
