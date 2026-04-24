# 🚀 Aigument

<img src="https://github.com/user-attachments/assets/cd11963a-5670-4f84-bab0-1fc0646154ea" width="100%" alt="Aigument Main Banner" />

> **AI + Argument (인수, 전달인자)**
> 유저 간 채팅 내용을 AI에게 전달하여 논리적 근거를 바탕으로 승패를 나누는 **토론 기반 분석 플랫폼**입니다.
<br>

## 📖 프로젝트 소개
**Aigument**는 단순한 채팅을 넘어, 대화의 맥락을 데이터화하고 AI 모델이 이를 분석하여 승자를 판정하는 혁신적인 토론 환경을 제공합니다.
<br>

* **실시간 양방향 소통**: WebSocket 및 STOMP 프로토콜을 활용한 저지연 채팅 시스템
* **데이터 파이프라인**: Redis를 메시지 브로커로 활용해 채팅 내용을 수집하고 AI 분석 엔진(Ollama)으로 전달
<br>

## 🛠 기술 스택 (Tech Stack)

### **Backend & Security**
* **Language**: Java 17
* **Framework**: Spring Boot 3.4.0
* **Security**: Spring Security, JWT, OAuth2 (Social Login)
* **Persistence**: Spring Data JPA (MySQL), Redis (RTopic, Pub/Sub)
<br>

### **Infrastructure & AI**
* **Cloud**: AWS EC2
* **Container**: Docker, Nginx (Reverse Proxy)
* **CI/CD**: GitHub Actions
* **AI Engine**: Ollama (LLM Integration)
<br>

### **Tools**
* Git / GitHub, Notion, Swagger (OpenAPI 3.0)
<br>

## ✨ 주요 기능 및 로드맵

### **✅ v1 - MVP (Completed)**
* **인증 및 인가**: JWT 기반 로컬 로그인 및 OAuth2 소셜 로그인 연동
* **커뮤니티**: 채팅방 생성, 상세 및 목록 조회, 실시간 입장/퇴장 시스템
* **메시징**: Redis RTopic 기반의 분산 메시지 브로커 환경 구축
* **AI 토론 분석**: OpenAI 및 Ollama 연동을 통한 실시간 승패 판정 로직
<br>

### **📅 v2 - Performance (2026.05 예정)**
* Redis 분산 락을 이용한 동시성 제어 및 서버 성능 고도화
* 시스템 아키텍처 고도화 및 최적화
<br>

### **📅 v3 - Additional Features (TBD)**
* 채팅방 검색 기능
* 유저 신고와 패널티 기능
* 관리자 1대1 문의 기능
* 랜덤 매칭 기능
<br>

### **📅 Testing (2026.06 예정)**
* JUnit5 기반 단위/통합 테스트 코드 작성 (커버리지 70% 목표)
<br>

## 🏗 시스템 아키텍처 (Architecture)
> *v2 브랜치 작업 완료 후 업데이트 예정*
<br>

## 🔳 와이어 프레임
<img width="3324" height="1729" alt="와이어 프레임" src="https://github.com/user-attachments/assets/a22d82bc-2a20-4be8-bd2e-bef6496f677b" />
<br>

## 📊 ERD (Entity Relationship Diagram)
| version | ERD |
| :---    | :---|
| **v1**  | <img src="https://github.com/user-attachments/assets/5fc22530-f1e2-4b52-bbe4-f9131506b51d" width="80%" alt="Aigument-ERD" /> |
| **v2**  | *추가예정*    |
| **v3**  | *추가예정*    |
<br>

## 📝 API 명세서
| 구분 | URL | 비고 |
| :--- | :--- | :--- |
| **Swagger UI** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | 포트 매핑 (8080:8080) |

> **Note**: Docker 실행 시 호스트와 컨테이너 간 포트 포워딩 설정하였습니다.

|**notion**| https://www.notion.so/Aigument-API-Specification-34c72de835a180ef9e19cd0c28ced4a9?source=copy_link | :--- |
<br>
