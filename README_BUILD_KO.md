# SKYBLOCKD (Java 11 / Minecraft 1.16.5) 빌드 안내

- **기능 보존**: 업로드하신 소스의 파일/구조/코드는 삭제/축소 없이 그대로 보존했습니다. (패키지/클래스/리소스 한 글자도 임의 변경 없음)
- **빌드 세팅**: Java 11, Spigot API 1.16.5 로 컴파일되도록 `pom.xml`을 패치했습니다.
- **주의**: 본 작업은 *빌드 스켈레톤 정리*에 집중했습니다. 외부 라이브러리(예: Vault, BentoBox 등)는 pom.xml에 이미 존재하면 그대로 유지했고, 없다면 프로젝트별 상황에 맞게 수동 추가가 필요할 수 있습니다.

## 빌드 방법
1. JDK 11 설치
2. Maven 3.8+ 설치
3. 프로젝트 루트에서:
   ```bash
   mvn -U clean package
   ```
4. 산출물: `target/*.jar`

## 마이그레이션 메모
- pom.xml detected and patched for Java 11 + Spigot 1.16.5.
- `plugin.yml`/`paper-plugin.yml`이 소스 트리 외부에 있었던 경우 `src/main/resources/`에도 복사해 두었습니다. (원본은 그대로 유지)
- 모듈/서브프로젝트가 여러 개인 경우, 각 모듈 별로 Java 11 설정을 추가해야 합니다.

## 문제 발생시
- 만약 컴파일 에러가 난다면, *기능을 바꾸지 않는 선*에서 다음을 점검해 주세요:
  - (1) pom.xml에 필요한 의존성(예: Vault, BentoBox, PlaceholderAPI 등) 추가
  - (2) 1.16.5 API에서 삭제/이동된 타입/메서드 사용 여부
  - (3) JDK 11에서 금지된 미사용 import/모듈 경고
