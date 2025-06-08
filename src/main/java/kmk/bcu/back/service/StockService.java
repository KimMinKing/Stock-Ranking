package kmk.bcu.back.service;


import kmk.bcu.back.config.KoreaInvestConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StockService {

    private final WebClient webClient;
    private final KoreaInvestConfig config;

    public Mono<String> getStockInfo(String stockCode) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/search-stock-info")
                        .queryParam("PRDT_TYPE_CD", "300")
                        .queryParam("PDNO", stockCode)
                        .build())
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + config.getAccessToken())
                .header("appkey", config.getAppkey())
                .header("appsecret", config.getAppsecret())
                .header("tr_id", config.getTrId())
                .header("custtype", "P")
                .retrieve()
                .bodyToMono(String.class);
    }
}
