
# MusicPlayer 🎵

안드로이드에서 동작하는 모듈화된 음악 플레이어 프로젝트입니다.  
Jetpack Compose와 MediaPlayer API를 활용하며, Clean Architecture 기반으로 설계되었습니다.

## 주요 기능 ✨
- **음악 재생 및 컨트롤**: MediaPlayer와 Notification Manager를 활용한 백그라운드 재생.
- **모듈화 구조**: 기능별로 분리된 모듈.
- **Compose UI**: Jetpack Compose를 통해 UI 구현.
- **DI (의존성 주입)**: Koin으로 모듈화된 의존성 관리.
- **데이터 흐름 관리**: Coroutines 및 Flow를 활용한 비동기 처리.

## 프로젝트 구조 📂

```
MusicPlayer/
├── app/                 # 애플리케이션 (UI 및 주요 기능 포함)
├── buildSrc/            # 빌드 스크립트와 플러그인 관리
├── common_config/       # 공통 설정 및 구성 파일
├── common_data/         # 공통 데이터 클래스 및 리소스
├── common_ui/           # 공통 UI 컴포넌트 및 테마 및 리소스
├── common_utils/        # 유틸리티 함수
├── core/                # 핵심 비즈니스 로직 처리
├── data/                # 데이터 소스, 리포지토리 및 모델
└── build.gradle.kts     # 프로젝트 빌드 설정 (Kotlin DSL)
```


## 기술 스택 🛠️
- **언어**: Kotlin
- **UI**: Jetpack Compose
- **의존성 주입**: Koin
- **비동기 처리**: Coroutines, Flow
- **미디어 재생**: MediaPlayer API
- **아키텍처**: Clean Architecture (MVVM 기반)

---
