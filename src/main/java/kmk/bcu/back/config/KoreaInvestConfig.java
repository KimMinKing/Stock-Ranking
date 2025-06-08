package kmk.bcu.back.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "korea-invest")
@Data
public class KoreaInvestConfig {
    private String baseUrl;
    private String appkey;
    private String appsecret;
    private String accessToken;
    private String trId;
}
