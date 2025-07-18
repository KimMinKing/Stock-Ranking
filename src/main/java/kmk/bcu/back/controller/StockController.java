package kmk.bcu.back.controller;

import kmk.bcu.back.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;


@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final WebClient webClient;
    private final TokenService tokenService;

    public StockController(WebClient.Builder builder, TokenService tokenService) {
        this.webClient = builder.baseUrl("https://openapi.koreainvestment.com:9443").build();
        this.tokenService = tokenService;
    }


    @Value("${koreainvestment.appkey}")
    private String appKey;

    @Value("${koreainvestment.appsecret}")
    private String appSecret;

    @GetMapping("/{code}")
    public ResponseEntity<String> getStockInfo(@PathVariable String code) {
        String accessToken = tokenService.getAccessToken();
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/search-stock-info")
                        .queryParam("PRDT_TYPE_CD", "300")
                        .queryParam("PDNO", code)
                        .build())
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", appKey)
                .header("appsecret", appSecret)
                .header("tr_id", "CTPF1002R")
                .header("custtype", "P")
                .header("Content-Type", "application/json; charset=utf-8")
                .retrieve()
                .toEntity(String.class)
                .block();
    }
}

