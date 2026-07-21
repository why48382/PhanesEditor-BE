# PhanesEditor Backend Project

![header](https://capsule-render.vercel.app/api?type=waving&color=0:8EC5FC,100:E0C3FC&height=200&section=header&text=Phanes%20Editor&fontSize=60&fontColor=ffffff&animation=fadeIn&fontAlignY=35&desc=Real-time%20Collaboration%20on%20Code&descAlignY=60&descAlign=50)

## 1. 프로젝트 소개

> Phanes Editor는 Spring Boot와 Vue3를 기반으로 개발한 실시간 협업 코드 에디터입니다.

> WebSocket과 STOMP를 이용한 실시간 코드 동기화와 JWT 기반 인증을 통해 여러 사용자가 하나의 프로젝트에서 함께 개발할 수 있는 환경을 제공합니다.

<!-- 배포 후 경로 등록하기 --> 

### 링크
[PhanesEditor 바로가기](https://www.phaneseditor.site/)

테스트 아이디  
ID: test  
PASSWORD: 1234

## 2. 프로젝트 정보

| 항목           | 내용                                        |
|----------------|-------------------------------------------|
| 개발 기간      | 2025-06-18 ~ 2025-09-23                   |
| 개발 인원      | 2명                                        |
| 담당 역할      | Backend, Frontend, Infra                  |
| 주요 기술 스택 | Spring Boot, Vue3, MariaDB, Docker, Nginx |

## 3. 주요 기능

### 👥 프로젝트 협업

- 프로젝트 생성 및 관리
- 프로젝트 멤버 초대 및 참여
- 프로젝트별 독립적인 작업 공간 제공

### 💻 실시간 코드 편집

- 여러 사용자가 동시에 코드 편집
- 변경 사항을 실시간으로 동기화
- 프로젝트 참여자에게 즉시 반영
- WebSocket 기반 실시간 통신

### 🔐 사용자 인증

- JWT 기반 로그인 및 인증
- 인증된 사용자만 프로젝트 접근 가능

### 📂 파일 관리

- 파일 및 디렉터리 생성
- 파일 수정 및 저장
- 프로젝트 구조 관리

## 4. 기술 스택

### frontend

![Vue.js](https://img.shields.io/badge/Vue_3-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socketdotio&logoColor=white)
![STOMP](https://img.shields.io/badge/STOMP-000000?style=for-the-badge&logoColor=white)

### Backend

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### infra

![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)

## 5. 시스템 아키텍처

![아키텍처 이미지](assets/Architecture/Architecture.png)
<!-- 이미지 변경 필요 -->

[//]: # (설명도 추가하자)

## 6. 트러블 슈팅

### 문제

프로젝트 상세 조회 API에서 Project, File, ProjectMember, User 정보를 각각 조회하면서 Hibernate의 LAZY Loading과 BatchSize에 의해 여러 개의 추가 SQL이
발생하였다.

특히 프로젝트 참여자가 많아질수록 IN (...) 형태의 조회가 반복되어 응답 속도가 크게 저하되었다.

### 원인 분석

기존에는 Project > Files > ProjectMember > User > ••• Chat

Project 조회 후 연관 엔티티를 LAZY Loading으로 조회하면서 추가 SQL이 반복 실행되었다.

@BatchSize는 N+1 문제를 일부 완화했지만 추가 SQL 자체는 제거하지 못했다.

### 해결

```java

@Query("""
        SELECT p
        FROM Project p
        LEFT JOIN FETCH p.fileList
        WHERE p.idx = :idx
        """)
Optional<Project> findByProjectIdx(Integer idx);
```

프로젝트와 파일을 한 번의 SQL로 조회하도록 변경하였다.

ProjectMember 부분 또한
프로젝트 멤버 조회 시 사용자 정보(닉네임, 프로필 등)가 항상 함께 사용되므로, User 엔티티를 즉시 조회하도록 변경하여 추가 SQL이 발생하지 않도록 개선하였다.

### 결과

<p align="center">
  <img src="assets/test/before.png" width="45%">
  <img src="assets/test/after.png" width="45%">
</p>

| 항목      |   개선 전 |  개선 후 |
|---------|-------:|------:|
| 평균 응답시간 | 2963ms | 754ms |
| RPS     |    195 |   381 |
| SQL 호출  |     다수 |    3회 |

## 7. 향후 개선 사항

- OT/CRDT 기반 충돌 해결
- 코드 실행기능
- 코드 변경 이력
- 권한 관리


[//]: # (GIF로 두개의 화면중 한개의 화면에서 로그인 프로젝트 생성 후 서로 웹소켓으로 통신되는 모습을 보여주면 됨)
[//]: # (배포 후 링크까지 걸어줘야 완성임 테스트 계정도 넣어주고)