FROM ox26n5h75wajyn.xuanyuan.run/library/maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /teacher-system

COPY settings.xml /root/.m2/settings.xml

COPY pom.xml .

COPY teacher-system-gateway/pom.xml teacher-system-gateway/
COPY teacher-system-common/pom.xml teacher-system-common/

RUN mvn -s /root/.m2/settings.xml -f teacher-system-gateway/pom.xml -DskipTests dependency:go-offline

COPY . .

RUN mvn -s /root/.m2/settings.xml -pl teacher-system-gateway -am -Pdev -DskipTests package

FROM ox26n5h75wajyn.xuanyuan.run/library/eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update && apt-get install -y curl bash dos2unix && rm -rf /var/lib/apt/lists/*

WORKDIR /teacher-system/gateway

RUN groupadd -r teacher && \
    useradd -r -g teacher -d /teacher-system -s /bin/bash teacher && \
    mkdir -p app config

COPY --from=build /teacher-system/teacher-system-gateway/target/*.jar ./app/gateway.jar

COPY config/dev.env ./config/dev.env

RUN tr -d '\r' < ./config/dev.env > ./config/dev.env.tmp && \
    mv ./config/dev.env.tmp ./config/dev.env && \
    sed -i '/^[[:space:]]*$/d' ./config/dev.env && \
    chown -R teacher:teacher /teacher-system && \
    chmod 644 ./config/dev.env && \
    chmod +x ./app/gateway.jar

USER teacher

EXPOSE 10001

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:10001/actuator/health || exit 1

ENTRYPOINT ["/bin/bash", "-c", "set -a && source ./config/dev.env && exec java -jar ./app/gateway.jar"]