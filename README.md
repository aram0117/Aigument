# 🚀 Aigument

<img width="1024" height="572" alt="image" src="https://github.com/user-attachments/assets/cd11963a-5670-4f84-bab0-1fc0646154ea" />

> AI 와 Argument(인수, 전달인자)의 합성어로 유저 간 채팅 시스템을 통해 대화 내용을 AI에게 전달하여 승패를 나누는 토론 기반 플랫폼입니다.



<br>
## 📖 프로젝트 소개
Websocket 과 Stomp 규격을 사용하여 양방향 소통이 가능한 채팅 시스템을 구축하였고
Redis를 활용하여 토론 내용을 수집해 분석하기 요청 시 AI로 전달 한 뒤 승패를 나누는 구조입니다.



<br>
## ✨ 주요 기능

# v1 - mvp
* **기능 1:** 회원가입, 로그인을 통한 인증/인가와 Oauth2를 도입한 소셜로그인 기능
* **기능 2:** 유저 관련 api (조회, 정보 수정, 승패 전적 추가)
* **기능 3:** 채팅방 api (생성, 상세 조회, 목록 조회, 입장, 퇴장)
* **기능 4:** 메시지 브로커(RTopic)와 Websocket을 활용한 이벤트 및 채팅 전송 기능
* **기능 5:** OpenAI를 활용한 토론 분석 기능

# v2 - 성능 개선 및 고도화
*2026/05 ~ 중 작업 예정* 

# v3 - 기능별 단위, 통합 테스트 코드 추가 (커버리지 단위당 70% 달성) 
*2026/06 ~ 중 작업 예정* 

# v4 - 부가 기능 추가
*추후에 작업 예정*



<br>
## 🛠 기술 스택 (Tech Stack)

### **Backend**
* Java 17
* Spring Boot 3.4.0
* Spring Security / JWT / Oauth2 / Spring Data JPA
* MySQL / Redis

### **Infrastructure & DevOps**
* AWS (EC2)
* Docker / Nginx
* GitHub Actions (CI/CD)
* AI (Oliama)
* Websocket / Stomp

### **Tools & Collaboration**
* Git / GitHub
* Notion
* Swagger (API Docs)




<br>
## 🏗 시스템 아키텍처 (Architecture)
*v2 브랜치 작업 후 작성 예정* 
 


<br>
## 📊 ERD
<img width="860" height="1644" alt="Aigument-ERD" src="https://github.com/user-attachments/assets/5fc22530-f1e2-4b52-bbe4-f9131506b51d" />



<br>

## 📝 API 명세서
Swagge : [http://localhost:8080/swagger-ui.html] 
> localhost와 docker-server 포트 매핑 설정하였음 (8080:8080)
