# SE_Term_Project_2026-1 - 이슈 관리 시스템 (IMS)

## 프로젝트 소개
본 프로젝트는 소프트웨어 공학(SE) 2026년 봄학기 텀 프로젝트로 개발된 **이슈 관리 시스템 (IMS)**입니다.
이슈의 등록, 관리, 검색 및 배정 프로세스를 체계적으로 지원하며, MVC 패턴을 적용하여 유연하고 확장성 있는 구조를 갖추고 있습니다.

## 주요 기능
- **계정 관리**: Admin, PL(Project Leader), Developer, Tester 권한별 기능 제공
- **이슈 생명 주기 관리**: 이슈 등록, 상태 변경(New -> Assigned -> Fixed -> Resolved -> Closed), 코멘트 추가
- **이슈 검색 및 필터링**: Assignee, Status, Reporter 등 다양한 조건으로 검색 가능
- **통계 분석**: 일별/월별 이슈 발생 현황 및 트렌드 분석
- **AI 기반 자동 배정 추천**: 기존 해결된 이슈 이력을 기반으로 최적의 담당자(Assignee) 추천

## 기술 스택
- **Language**: Java 17
- **Architecture**: MVC (Model-View-Controller)
- **UI Frameworks**: JavaFX, Swing (다중 인터페이스 지원)
- **Persistence**: JSON File System (Jackson 라이브러리 활용)
- **Build Tool**: Gradle
- **Testing**: JUnit 5

## 실행 방법 (환경별 안내)

### 1. Gradle이 설치된 환경 (추천)
저장소를 클론한 후, Gradle을 사용하여 빌드 및 실행합니다.
```bash
./gradlew run
```

### 2. Gradle이 없는 환경 (수동 실행)
빌드 도구가 없는 환경에서도 다음 스크립트를 통해 라이브러리 설정 및 테스트 실행이 가능합니다.
- **라이브러리 다운로드**: Jackson 및 JUnit 라이브러리를 `libs` 폴더로 다운로드합니다.
  ```bash
  bash setup_libs.sh
  ```
- **컴파일 및 테스트 실행**: 모델 로직과 시나리오 테스트를 수행합니다.
  ```bash
  bash compile_and_test.sh
  ```

### 3. 간편 실행 스크립트 (run.sh)
라이브러리 설정, 컴파일, 애플리케이션 실행을 한 번에 수행합니다.
```bash
bash run.sh
```

## 프로젝트 구조
- `src/main/java`: 핵심 로직 (Model, Controller, View Interface, SwingView)
- `src/main/resources/data`: 영속성 데이터를 위한 JSON 저장소
- `src/test/java`: JUnit 테스트 코드 (ScenarioTest)
- `libs`: 외부 라이브러리 (Jackson, JUnit) - `setup_libs.sh`로 생성 가능

## 프로젝트 팀원
- 팀원 정보 기재 예정