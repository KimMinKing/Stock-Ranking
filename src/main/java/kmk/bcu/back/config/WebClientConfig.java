package kmk.bcu.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// API가 배열을 output 키로 감싸서 보내므로, 배열은 data.output입니다.
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient(KoreaInvestConfig config) {
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();
    }
}
