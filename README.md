# SortStockDashboard

종목 정보를 정렬 및 조회할 수 있는 간단한 웹 애플리케이션입니다.  
한국투자증권(KIS) Open API를 활용해 주식 종목 데이터를 불러오고, 순위정렬 기능을 제공합니다.  

## 기술 스택

- **Backend**: Java 17, Spring Boot
- **Frontend**: 정적 HTML/CSS/JS (resources/static 디렉토리에 위치)
- **API**: 한국투자증권 Open API (KIS)
- **Build Tool**: Gradle

## 주요 기능

- 종목명, 현재가 등 다양한 항목을 기준으로 **정렬** 가능
- 간단한 분,일,주,월 봉의 차트 제공공

## 프로젝트 구조

```bash
src/
 └── main/
     ├── java/               # Spring Boot 백엔드
     │   └── kmk.bcu.back/
     ├── resources/
     │   ├── static/         # 프론트엔드 HTML/JS/CSS
     │   └── application.properties  # API 설정
````

## 설정 방법

1. 한국투자증권(KIS) Open API에서 **발급받은 AppKey, AppSecret, AppToken**을 `application.properties`에 작성해야 합니다.
2. 기본 토큰 발급 및 조회 요청을 위해 다음 항목이 필요합니다:

```properties
koreainvestment.appkey=YOUR_APP_KEY
koreainvestment.appsecret=YOUR_APP_SECRET
koreainvestment.accesstoken=YOUR_ACCOUNT_NUMBER
```

> ⚠️ 주의: 이 파일(`application.properties`)은 절대로 공개 저장소에 업로드하지 마세요.
> 민감 정보가 포함되어 있으며, `.gitignore`에 추가되어 있어야 합니다.

## 실행 방법

1. KIS API 키 발급 및 설정 완료
2. 아래 명령어로 실행

```bash
./gradlew bootRun
```

3. 웹 브라우저에서 `http://localhost:8080` 접속

---

## 라이선스

MIT License

```

---

필요하면 템플릿 기반으로 더 길게 확장도 가능합니다.  
README에 이미지나 차트가 추가되면, `assets` 폴더 만들어서 링크로 삽입하면 됩니다.
```
