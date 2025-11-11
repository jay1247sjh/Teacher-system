FROM library/maven:3.9.6-eclipse-temurin-21 AS build

# 定义构建参数，默认为 dev 环境
ARG BUILD_ENV=dev

WORKDIR /teacher-system

COPY settings.xml /root/.m2/settings.xml

COPY pom.xml .

COPY teacher-system-common/pom.xml teacher-system-common/
COPY teacher-system-table/pom.xml teacher-system-table/
COPY teacher-system-table/teacher-system-table-controller/pom.xml teacher-system-table/teacher-system-table-controller/
COPY teacher-system-table/teacher-system-table-application/pom.xml teacher-system-table/teacher-system-table-application/
COPY teacher-system-table/teacher-system-table-domain/pom.xml teacher-system-table/teacher-system-table-domain/
COPY teacher-system-table/teacher-system-table-infrastructure/pom.xml teacher-system-table/teacher-system-table-infrastructure/

RUN mvn -s /root/.m2/settings.xml -f teacher-system-table/pom.xml -DskipTests dependency:go-offline

COPY . .

RUN mvn -s /root/.m2/settings.xml -pl teacher-system-table/teacher-system-table-controller -am -P${BUILD_ENV} -DskipTests package

FROM library/eclipse-temurin:21-jre-jammy AS runtime

# 传递环境变量到运行阶段
ARG BUILD_ENV=dev
ENV APP_ENV=${BUILD_ENV}

RUN apt-get update && apt-get install -y curl bash dos2unix && rm -rf /var/lib/apt/lists/*

WORKDIR /teacher-system/table

RUN groupadd -r teacher && \
    useradd -r -g teacher -d /teacher-system -s /bin/bash teacher && \
    mkdir -p app config attachments

COPY --from=build /teacher-system/teacher-system-table/teacher-system-table-controller/target/*.jar ./app/table.jar

COPY config/${APP_ENV}.env ./config/app.env

RUN tr -d '\r' < ./config/app.env > ./config/app.env.tmp && \
    mv ./config/app.env.tmp ./config/app.env && \
    sed -i '/^[[:space:]]*$/d' ./config/app.env && \
    chown -R teacher:teacher /teacher-system && \
    chmod 644 ./config/app.env && \
    chmod +x ./app/table.jar

USER teacher

EXPOSE 10003

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:10003/actuator/health || exit 1

ENTRYPOINT ["/bin/bash", "-c", "set -a && source ./config/app.env && exec java -jar ./app/table.jar"]

