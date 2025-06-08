package kmk.bcu.back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.Map;

@Service
public class StockPriceService {


    private final RestTemplate restTemplate;
    private final String baseUrl = "https://openapi.koreainvestment.com:9443";
    private final String trId = "FHKST01010200"; // 데일리 시세 TR ID 예시

    private final String appKey = "발급받은 appkey";
    private final String appSecret = "발급받은 appsecret";
    private final String accessToken = "발급받은 access token"; // OAuth2로 발급

    public StockPriceService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String getDailyPrice(String stockCode) {
        String url = baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-daily-price";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", trId);
        headers.set("custtype", "P"); // 개인고객

        Map<String, String> params = new HashMap<>();
        params.put("FID_INPUT_ISCD", stockCode);
        params.put("FID_COND_MRKT_DIV_CODE", "J"); // KRX 시장

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
        }
    }
}
