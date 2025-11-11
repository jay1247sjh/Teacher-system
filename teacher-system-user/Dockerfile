FROM library/maven:3.9.6-eclipse-temurin-21 AS build

# 定义构建参数，默认为 dev 环境
ARG BUILD_ENV=dev

WORKDIR /teacher-system

COPY settings.xml /root/.m2/settings.xml

COPY pom.xml .

COPY teacher-system-common/pom.xml teacher-system-common/
COPY teacher-system-user/pom.xml teacher-system-user/
COPY teacher-system-user/teacher-system-user-controller/pom.xml teacher-system-user/teacher-system-user-controller/
COPY teacher-system-user/teacher-system-user-application/pom.xml teacher-system-user/teacher-system-user-application/
COPY teacher-system-user/teacher-system-user-domain/pom.xml teacher-system-user/teacher-system-user-domain/
COPY teacher-system-user/teacher-system-user-infrastructure/pom.xml teacher-system-user/teacher-system-user-infrastructure/

RUN mvn -s /root/.m2/settings.xml -f teacher-system-user/teacher-system-user-controller/pom.xml -DskipTests dependency:go-offline

COPY . .

RUN mvn -s /root/.m2/settings.xml -pl teacher-system-user/teacher-system-user-controller -am -P${BUILD_ENV} -DskipTests package

FROM library/eclipse-temurin:21-jre-jammy AS runtime

# 传递环境变量到运行阶段
ARG BUILD_ENV=dev
ENV APP_ENV=${BUILD_ENV}

RUN apt-get update && apt-get install -y curl bash dos2unix && rm -rf /var/lib/apt/lists/*

WORKDIR /teacher-system/user

RUN groupadd -r teacher && \
    useradd -r -g teacher -d /teacher-system -s /bin/bash teacher && \
    mkdir -p app config attachments

COPY --from=build /teacher-system/teacher-system-user/teacher-system-user-controller/target/*.jar ./app/user.jar

COPY config/${APP_ENV}.env ./config/app.env

RUN tr -d '\r' < ./config/app.env > ./config/app.env.tmp && \
    mv ./config/app.env.tmp ./config/app.env && \
    sed -i '/^[[:space:]]*$/d' ./config/app.env && \
    chown -R teacher:teacher /teacher-system && \
    chmod 644 ./config/app.env && \
    chmod +x ./app/user.jar

USER teacher

EXPOSE 10002

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:10002/actuator/health || exit 1

ENTRYPOINT ["/bin/bash", "-c", "set -a && source ./config/app.env && exec java -jar ./app/user.jar"]

