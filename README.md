# SFTP CLI Tool

K8s pod 등 SSH 클라이언트 없이 Java만 설치된 환경에서 SFTP 작업을 수행하기 위한 CLI 도구.

## 빌드

```bash
./gradlew bootJar
```

결과물: `build/libs/sftp.jar`

## 명령어

### 도움말

```bash
java -jar sftp.jar --help
java -jar sftp.jar <command> --help
```

### 공통 옵션

| 옵션 | 설명 | 필수 | 기본값 |
|------|------|------|--------|
| `-H, --host` | SFTP 서버 호스트 | O | - |
| `-P, --port` | SFTP 서버 포트 | X | 22 |
| `-u, --user` | 사용자명 | O | - |
| `-p, --password` | 비밀번호 | O | - |

### ls — 디렉토리 목록 조회

```bash
java -jar sftp.jar ls -H 10.0.0.1 -u admin -p secret /remote/path
```

정렬 옵션 (`--sort`):

```bash
# 오래된 순 (기본값)
java -jar sftp.jar ls --sort asc -H 10.0.0.1 -u admin -p secret /remote/path

# 최신 순
java -jar sftp.jar ls --sort desc -H 10.0.0.1 -u admin -p secret /remote/path
```

정규식 필터 (`--regex`):

```bash
# .txt 파일만 조회
java -jar sftp.jar ls --regex ".*\.txt" -H 10.0.0.1 -u admin -p secret /remote/path

# .log 파일만 최신 순으로 조회
java -jar sftp.jar ls --regex ".*\.log" --sort desc -H 10.0.0.1 -u admin -p secret /remote/path
```

리다이렉트로 파일 저장 가능:

```bash
java -jar sftp.jar ls -H 10.0.0.1 -u admin -p secret /remote/path > filelist.txt
```

출력 형식:

```
d       4096 2026-03-10 12:00:00 some-directory
f      12345 2026-03-10 12:30:00 some-file.txt
```

- `d` = 디렉토리, `f` = 파일

### get — 파일 다운로드

단일 파일:

```bash
java -jar sftp.jar get -H 10.0.0.1 -u admin -p secret /remote/file.txt ./local.txt
```

regex로 여러 파일 다운로드:

```bash
# .txt 파일 모두 다운로드
java -jar sftp.jar get --regex ".*\.txt" -H 10.0.0.1 -u admin -p secret /remote/dir/ ./local/dir/

# .log 또는 .csv 파일 다운로드
java -jar sftp.jar get --regex ".*\.(log|csv)" -H 10.0.0.1 -u admin -p secret /remote/dir/ ./local/dir/
```

`--regex` 사용 시 첫 번째 인자는 원격 **디렉토리**, 두 번째 인자는 로컬 **디렉토리**로 동작합니다.

### put — 파일 업로드

```bash
java -jar sftp.jar put -H 10.0.0.1 -u admin -p secret ./local.txt /remote/file.txt
```

### rm — 파일 삭제

```bash
# 확인 프롬프트 표시 (y 입력 시 삭제)
java -jar sftp.jar rm -H 10.0.0.1 -u admin -p secret /remote/file.txt

# -f 옵션으로 확인 없이 삭제
java -jar sftp.jar rm -f -H 10.0.0.1 -u admin -p secret /remote/file.txt
```

## 기술 스택

- Spring Boot 4.0.3 + Java 17 + Gradle
- Apache MINA SSHD — 순수 Java SFTP 클라이언트
- picocli — CLI 프레임워크
