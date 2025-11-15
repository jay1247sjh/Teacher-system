FROM ox26n5h75wajyn.xuanyuan.run/library/maven:3.9.6-eclipse-temurin-21 AS build

# 定义构建参数，默认为 dev 环境
ARG BUILD_ENV=dev

WORKDIR /teacher-system

COPY settings.xml /root/.m2/settings.xml

# 安装本地 JAR 到 Maven 仓库
COPY lib/jwt-utils-1.0.0.jar /tmp/jwt-utils-1.0.0.jar
RUN mvn install:install-file \
    -Dfile=/tmp/jwt-utils-1.0.0.jar \
    -DgroupId=sjh.jwt \
    -DartifactId=jwt-utils \
    -Dversion=1.0.0 \
    -Dpackaging=jar

COPY pom.xml .

# 安装父 POM
RUN mvn -s /root/.m2/settings.xml -N install

COPY teacher-system-common/pom.xml teacher-system-common/
COPY teacher-system-user/pom.xml teacher-system-user/
COPY teacher-system-user/teacher-system-user-controller/pom.xml teacher-system-user/teacher-system-user-controller/
COPY teacher-system-user/teacher-system-user-application/pom.xml teacher-system-user/teacher-system-user-application/
COPY teacher-system-user/teacher-system-user-domain/pom.xml teacher-system-user/teacher-system-user-domain/
COPY teacher-system-user/teacher-system-user-infrastructure/pom.xml teacher-system-user/teacher-system-user-infrastructure/

# 先安装 common 模块
COPY teacher-system-common/ teacher-system-common/
RUN mvn -s /root/.m2/settings.xml -f teacher-system-common/pom.xml -DskipTests install

RUN mvn -s /root/.m2/settings.xml -f teacher-system-user/pom.xml -DskipTests dependency:go-offline

COPY . .

RUN mvn -s /root/.m2/settings.xml -pl teacher-system-user/teacher-system-user-controller -am -P${BUILD_ENV} -DskipTests package

FROM ox26n5h75wajyn.xuanyuan.run/library/eclipse-temurin:21-jre-jammy AS runtime

# 传递环境变量到运行阶段
ARG BUILD_ENV=dev
ENV APP_ENV=${BUILD_ENV}

RUN apt-get update && apt-get install -y curl bash dos2unix && rm -rf /var/lib/apt/lists/*

WORKDIR /teacher-system/user

RUN groupadd -r teacher && \
    useradd -r -g teacher -d /teacher-system -s /bin/bash teacher && \
    mkdir -p /teacher-system/nacos/snapshot && \
    mkdir -p /teacher-system/nacos/logs && \
    mkdir -p /teacher-system/nacos/config && \
    chown -R teacher:teacher /teacher-system/nacos

COPY --from=build /teacher-system/teacher-system-user/teacher-system-user-controller/target/*.jar ./app/user.jar

USER teacher

EXPOSE 10002

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:10002/actuator/health || exit 1

ENTRYPOINT ["java", "-Dnacos.local.snapshot=/teacher-system/nacos/snapshot", "-Dnacos.home=/teacher-system/nacos", "-Dnacos.logging.path=/teacher-system/nacos/logs", "-Dnacos.config.cache.dir=/teacher-system/nacos/config", "-jar", "./app/user.jar"]
