# 윤아리 놀이터 Android

Android 8.0 이상에서 실행하는 설치용 시험판입니다. 게임 화면과 그림은 앱에 포함되며 광고, 로그인, 외부 웹 탐색 기능이 없습니다.

## 설치

저장소 Releases에서 Yunari-Play.apk를 휴대폰으로 내려받아 엽니다. 아직 Google Play 배포판이 아니며 기기에서 브라우저의 앱 설치 허용을 요청할 수 있습니다.

## 빌드

JDK 17, Gradle 8.11.1, Android SDK 35 / Build Tools 35.0.0.
android 디렉터리에서 `gradle :app:assembleDebug :app:lintDebug`.
main의 android 파일 변경 시 GitHub Actions가 APK와 시험판 다운로드를 생성합니다.

## 음성과 검증 범위

기기의 한국어/영어 TextToSpeech 음성 데이터가 필요합니다. 가능한 고품질 오프라인 음성을 선택합니다. 성우 녹음 또는 외부 생성 음원은 아직 포함하지 않았습니다.
자동 빌드는 컴파일과 Android lint를 확인합니다. 실제 휴대폰의 발음, 터치, 음량은 기기에서 확인해야 합니다.
디버그 서명은 빌드 캐시에 보관합니다. 캐시가 삭제되면 서명이 달라질 수 있으므로 정식 배포에는 별도 보호된 릴리스 서명이 필요합니다.
