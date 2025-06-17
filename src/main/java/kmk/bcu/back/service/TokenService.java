package kmk.bcu.back.service;


import kmk.bcu.back.dto.TokenResponse;
import kmk.bcu.back.entity.AccessToken;
import kmk.bcu.back.repository.AccessTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {

    @Value("${koreainvestment.appkey}")
    private String appKey;

    @Value("${koreainvestment.appsecret}")
    private String appSecret;

    private final AccessTokenRepository accessTokenRepository;
    private final WebClient webClient;

    public TokenService(AccessTokenRepository accessTokenRepository, WebClient.Builder webClientBuilder) {
        this.accessTokenRepository = accessTokenRepository;
        this.webClient = webClientBuilder.baseUrl("https://openapi.koreainvestment.com:9443").build();
    }

    public String getAccessToken() {
        AccessToken token = accessTokenRepository.findTopByOrderByIdDesc()
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseGet(this::generateNewToken);

        return token.getToken();
    }

    private AccessToken generateNewToken() {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);

        TokenResponse response = webClient.post()
                .uri("/oauth2/tokenP")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();

        AccessToken newToken = new AccessToken();
        newToken.setToken(response.getAccess_token());
        newToken.setExpiresAt(LocalDateTime.now().plusSeconds(response.getExpires_in()));

        return accessTokenRepository.save(newToken);
    }
}
