<div align="center">

<h1>ShBlog</h1>
<p>마크다운 기반 개인 기술 블로그 플랫폼</p>

<p>
  <a href="https://github.com/highcera/shblog-backend">
    <img src="https://img.shields.io/badge/Backend%20Repo-181717?style=for-the-badge&logo=github&logoColor=white" alt="Backend Repo">
  </a>
</p>

</div>

---

## 목차
- [서비스 소개](#서비스-소개)
- [기술 스택](#기술-스택)
- [핵심 기능](#핵심-기능)
- [기술적 특징](#기술적-특징)
- [ERD](#erd)
- [시스템 아키텍처](#시스템-아키텍처)

---

## 서비스 소개

ShBlog는 개발 학습 과정과 기술 기록을 정리하기 위해 만든 **개인 기술 블로그 플랫폼**입니다.

마크다운 기반 게시글 작성, 카테고리 분류, 이미지 업로드, 검색 기능을 제공하며  
Spring Boot 기반 백엔드와 Next.js 기반 프론트엔드로 구성된 **풀스택 블로그 서비스**입니다.

주요 목표  

- 마크다운 기반 기술 블로그 운영  
- 카테고리 기반 글 관리  
- 이미지 업로드 및 파일 관리 자동화  
- Docker 기반 서버 배포 자동화  

---

## 기술 스택

### Backend

<img src="https://img.shields.io/badge/Java_17-007396?style=for-the-badge&logo=openjdk&logoColor=white"> 
<img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">

<img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/QueryDSL-0769AD?style=for-the-badge">

### Database & Storage

<img src="https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white">
<img src="https://img.shields.io/badge/MinIO(S3%20Compatible)-C72E49?style=for-the-badge">

### Infra & DevOps

<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
<img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
<img src="https://img.shields.io/badge/Ubuntu_Server-E95420?style=for-the-badge&logo=ubuntu&logoColor=white">

---

## 핵심 기능

### 1. 블로그 게시글 작성 (Markdown)

마크다운 기반 게시글 작성 기능

- 제목 / 본문 작성
- 공개 여부 설정
- 카테고리 선택
- 이미지 삽입 지원

---

### 2. 게시글 조회 및 검색

QueryDSL 기반 게시글 조회 기능

- 사용자 블로그 게시글 목록 조회
- 카테고리별 게시글 조회
- 키워드 검색 (제목 + 내용)

---

### 3. 카테고리 관리

블로그별 카테고리 구조 제공

- 카테고리 생성
- 카테고리 수정
- 카테고리 삭제
- 게시글 작성 시 카테고리 선택

---

### 4. 이미지 업로드

MinIO(S3 compatible)를 이용한 이미지 업로드

- 이미지 파일 업로드
- 게시글 본문 이미지 삽입
- 게시글 수정 시 미사용 파일 자동 정리

---

### 5. 게시글 관리

로그인 사용자는 자신의 게시글을 관리할 수 있습니다.

- 게시글 수정
- 카테고리 변경
- 공개/비공개 전환
- 게시글 삭제

---

## 기술적 특징

- **QueryDSL 동적 쿼리**  
  닉네임 / 카테고리 / 키워드 조건을 조합한 게시글 검색 및 필터링 기능 구현

- **파일 상태 기반 관리 구조**  
  UploadFile 상태(TEMP, ATTACHED, DELETED)를 관리하여 게시글과 파일의 연결 상태 추적

- **고아 파일 방지 로직**  
  게시글 수정 시 본문에서 제거된 이미지 파일을 감지하여 MinIO 객체 삭제 및 상태 업데이트 처리

- **Spring Batch 기반 파일 정리**  
  TEMP 및 DELETED 상태 파일을 주기적으로 정리하여 스토리지와 DB 간 파일 상태를 일관되게 유지

- **Docker 기반 서버 실행 환경**  
  백엔드 애플리케이션을 Docker 컨테이너로 구성하여 서버 배포 및 실행 환경 단순화

- **GitHub Actions CI/CD 파이프라인**  
  코드 푸시 시 자동 빌드 및 Docker 기반 서버 배포 자동화

---

## ERD

<div align="center">

<img width="1480" height="742" alt="shBlog" src="https://github.com/user-attachments/assets/bdb0d03b-bdac-4931-b515-4075c83000cc" />

</div>

---

## 시스템 아키텍처

<div align="center">

<img width="534" height="446" alt="architecture" src="https://github.com/user-attachments/assets/bd2d6c2d-8abd-461e-ba8b-6d19db097116" />

</div>
