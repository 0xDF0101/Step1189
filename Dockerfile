# step01 build
# maven:3.9-eclipse-temurin-21 = Maven 3.9 + Temurin JDK 21이 미리 설치된 무거운 빌드용 이미지
# AS builder → 이 스테이지에 이름표를 붙임. 최종 이미지엔 안 남고, 아래 COPY --from=builder로 결과물만 꺼내 쓰기 위함
FROM maven:3.9-eclipse-temurin-21 AS builder

# 이후 명령어들이 실행될 컨테이너 내부 기준 디렉터리를 /app으로 설정 (없으면 자동 생성)
WORKDIR /app

# 로컬(빌드 컨텍스트) 전체를 컨테이너 /app으로 복사
# COPY <컨텍스트 기준 경로> <컨테이너 기준 경로> → 여긴 둘 다 "현재 위치 전체"라 프로젝트 전체가 그대로 들어감
COPY . .

# mvn clean(이전 빌드 찌꺼기 target/ 삭제) → package(컴파일+테스트+jar 패키징, 앞단계 전부 포함)
# -DskipTests → 테스트 실행만 건너뜀(컴파일은 함) → 빌드 시간 단축
# 결과물: /app/target/*.jar 생성
RUN mvn clean package -DskipTests

# step02  execute (JRE)
# 여기서부터 완전히 새로운 스테이지 시작 — 위 builder의 파일시스템은 기본적으로 다 버려짐
# eclipse-temurin = JDK 아니라 실행 전용 JRE 베이스, maven 없어서 훨씬 가벼움
# (이름이 AS build지만 실제로는 "실행/런타임" 스테이지 — build보다는 runtime이 더 맞는 이름)
FROM eclipse-temurin AS build

# 이번 스테이지(런타임 이미지) 안에서의 작업 디렉터리. builder의 /app과는 별개의 새 파일시스템
WORKDIR /app

# --from=builder → 방금 만든 이 스테이지가 아니라 위에서 이름 붙인 builder 스테이지의 파일시스템에서 가져옴
# /app/target/*.jar → builder가 mvn package로 만든 jar (와일드카드라 정확한 파일명 몰라도 됨)
# 이 한 줄이 두 스테이지를 잇는 유일한 연결고리 — maven/소스코드/.git 등 builder의 나머지는 전부 최종 이미지에 안 들어감
COPY --from=builder /app/target/*.jar app.jar

# 컨테이너 시작 시 실행할 명령 고정: java -jar app.jar
# 배열(exec) 형태라 셸을 안 거치고 자바 프로세스가 PID 1로 직접 실행됨
# → docker stop이 보내는 SIGTERM을 앱이 바로 받아서 정상 종료(graceful shutdown) 가능
ENTRYPOINT ["java", "-jar", "app.jar"]

# 이 컨테이너가 8080 포트를 쓴다는 문서화용 메타데이터. 실제로 포트를 열어주진 않음
# (포트 매핑은 docker-compose.yml의 expose/ports가 담당, 여긴 참고 정보일 뿐)
EXPOSE 8080
