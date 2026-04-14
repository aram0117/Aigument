# 🚀 프로젝트 이름 : Aigument

<img width="1024" height="572" alt="image" src="https://github.com/user-attachments/assets/cd11963a-5670-4f84-bab0-1fc0646154ea" />

> **한 줄 소개 : ** AI 와 Argument(인수, 전달인자)의 합성어로 유저 간 채팅 시스템을 통해 대화 내용을 AI에게 전달하여 승패를 나누는 토론 기반 플랫폼입니다.

<br>

## 📖 프로젝트 소개
Websocket 과 Stomp 규격을 사용하여 양방향 소통이 가능한 채팅 시스템을 구축하였고
Redis를 활용하여 토론 내용을 수집해 분석하기 요청 시 AI로 전달 한 뒤 승패를 나누는 구조입니다.
<br>

## ✨ 주요 기능
* **기능 1:** 회원가입, 로그인을 통한 인증/인가와 Oauth2를 도입한 소셜로그인 기능
* **기능 2:** 유저 관련 api (조회, 정보 수정, 승패 전적 추가)
* **기능 3:** 메시지 브로커(RTopic)와 Websocket을 활용한 채팅 전송 기능
* **기능 4:** OpenAI를 활용한 토론 분석 기능

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

// 추후에 작성 예정
## 🏗 시스템 아키텍처 (Architecture)
*(시스템 인프라 및 아키텍처 구조도를 여기에 삽입하세요.)*
![Architecture](이미지 URL)

<br>

## 📊 ERD
*(데이터베이스 ERD 이미지를 여기에 삽입하세요.)*
![ERD](이미지 URL)

<br>

## 📝 API 명세서
API 상세 명세는 아래 링크에서 확인할 수 있습니다.
* [Swagger API Docs 링크](URL)
* [또는 Notion API 명세서 링크](URL) //
