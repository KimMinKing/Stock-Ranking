package kmk.bcu.back.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class AccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 길이를 지정해주거나 TEXT로 명시
    @Column(length = 1000) // 또는 @Column(columnDefinition = "TEXT")
    private String token;

    private LocalDateTime expiresAt;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
