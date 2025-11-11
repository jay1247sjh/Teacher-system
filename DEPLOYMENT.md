### 部署文档 - Teacher System

---

### 1. 基础设施部署

在部署 Teacher System 项目之前，你需要确保你的服务器或开发环境已正确配置了以下基础设施组件。

#### 1.1 Docker 和 Docker Compose 安装

Docker 和 Docker Compose 是运行 Teacher System 的核心工具。

##### 1.1.1 安装 Docker Engine

根据你的操作系统，参考 Docker 官方文档进行安装：
*   **Linux**: [Install Docker Engine on Ubuntu](https://docs.docker.com/engine/install/ubuntu/) (或其他 Linux 发行版)
*   **macOS**: [Install Docker Desktop on Mac](https://docs.docker.com/desktop/install/mac-install/)
*   **Windows**: [Install Docker Desktop on Windows](https://docs.docker.com/desktop/install/windows-install/)

安装完成后，可以通过以下命令验证 Docker 是否成功安装：

```bash
docker --version
docker compose version
```

##### 1.1.2 Docker Compose 安装 (如果未随 Docker Desktop 安装)

如果你的 Docker 环境没有自带 Docker Compose V2 (即 `docker compose` 命令)，你需要单独安装它。如果 `docker compose version` 命令有效，则无需额外安装。

通常，Docker Desktop 会捆绑 Docker Compose。对于 Linux 服务器，你可能需要手动安装：

```bash
# 适用于 Linux，通常推荐使用curl下载
sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 如果你使用的是较旧的Docker Compose V1，命令是 docker-compose (带连字符)
# 但本项目推荐使用 V2，即 docker compose (无连字符)
```

#### 1.2 脚本执行权限

在 Linux/macOS 系统中，`.sh` 脚本文件默认可能没有执行权限。如果你遇到 `权限不够 (Permission denied)` 的错误，你需要为脚本添加执行权限：

```bash
chmod +x ./deploy.sh
chmod +x ./start-infra.sh
# 对其他 .sh 脚本也可能需要执行此操作
```

#### 1.3 部署基础服务 (MySQL, Nacos, Prometheus)

在完整项目部署之前，你可以选择先启动基础设施服务，以验证环境是否正常。

项目提供了两种方式来启动基础设施：使用 `./deploy.sh` 脚本（启动所有服务）或使用 `./start-infra.sh` 脚本（仅启动基础设施）。

##### 1.2.1 创建必要的目录

项目需要一些持久化存储目录，用于 MySQL 数据、Nacos 配置和 Prometheus 数据。`deploy.sh` 和 `start-infra.sh` 脚本都会自动创建这些目录。

##### 1.2.2 启动基础服务

你可以选择以下两种方式来启动基础服务：

1.  **使用 `./start-infra.sh` (推荐用于开发)**:
    这个脚本专门用于只启动 `mysql`、`nacos` 和 `prometheus` 等基础设施服务。这在开发应用服务时非常有用，可以节省资源和时间。
    
    ```bash
    # 启动开发环境的基础设施
    ./start-infra.sh
    
    # 启动生产环境的基础设施
    BUILD_ENV=prod ./start-infra.sh
    ```
    
    **脚本功能**: 检查 Docker 环境，创建必要的目录，并使用 `docker compose up -d mysql nacos prometheus` 启动指定的容器。

2.  **使用 `./deploy.sh start` (启动所有服务，包括基础服务和应用服务)**:
    如果你希望一次性启动所有服务（包括 Gateway、User Service、Table Service 和 Web Frontend），可以使用主部署脚本。
    
    ```bash
    ./deploy.sh start
    ```

**关键点：**

*   **MySQL 初始化**: 项目中的 `config/mysql-deploy/1-init.sql` 文件会在 MySQL 容器首次启动时自动执行，用于创建 `teacher-system` 和 `nacos_config` 数据库、初始化 `teacher-system` 的数据库表和角色/权限数据，并创建 `txq` 用户及授予 `nacos_config` 数据库的权限。请确保该文件内容正确且完整。
*   **Nacos 配置**: Nacos 依赖于 MySQL 存储配置。`docker-compose.yaml` 中已配置 Nacos 连接到 `teacher-system-mysql` 服务。
    *   **Nacos 数据库模式**: `config/nacos-deploy/2-nacos-mysql-schema.sql` 文件包含了 Nacos 服务所需的数据库表结构。此脚本通常由 Nacos 容器在首次启动并连接到空 `nacos_config` 数据库时自动执行。确保此文件与你的 Nacos 版本兼容。

#### 1.3 验证基础设施状态

*   **Docker 容器状态**:
    ```bash
    docker ps
    ```
    确保 `teacher-system-mysql` 和 `teacher-system-nacos` 容器处于 `Up` 状态。
*   **MySQL 连接**:
    你可以尝试连接到 MySQL 容器，验证数据库和表是否已创建。
    ```bash
    docker exec -it teacher-system-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} teacher-system
    # 在MySQL命令行中执行：
    SHOW TABLES;
    SELECT * FROM `role`;
    ```
*   **Nacos 控制台**:
    访问 Nacos 控制台（默认 `http://localhost:8848/nacos`），使用 `nacos/nacos` 登录，检查服务列表和配置管理。

### 2. 完整项目部署

基础设施就绪后，你可以部署 Teacher System 的所有应用服务（Gateway, User Service, Table Service, Web Frontend）。

#### 2.1 环境变量配置

环境变量管理对项目的正确运行至关重要。

*   **后端服务环境变量**: 后端服务的环境变量定义在 `config/dev.env` 和 `config/prod.env` 文件中。`docker-compose.yaml` 通过 `env_file` 指令在容器启动时加载这些文件。
    *   `config/dev.env`: 用于开发环境的配置。
    *   `config/prod.env`: 用于生产环境的配置。
    你可以通过设置 `BUILD_ENV` 环境变量来选择使用哪个环境文件，例如：
    ```bash
    # 使用开发环境配置
    export BUILD_ENV=dev
    ./deploy.sh start

    # 使用生产环境配置
    export BUILD_ENV=prod
    ./deploy.sh start
    ```
    如果没有设置 `BUILD_ENV`，它将默认为 `dev`。

*   **前端服务环境变量**: 前端项目 (teacher-system-web) 使用 Vite 的环境变量。
    *   在 `teacher-system-web/.env.development` (或 `.env.production` 等) 文件中配置前端特有的环境变量，例如 `VITE_ATTACHMENT_BASE_URL`。
    *   Vite 会在构建时将这些变量注入到客户端代码中。

#### 2.2 部署脚本使用 (`deploy.sh` 和 `start-infra.sh`)

项目提供两个主要的部署脚本：`deploy.sh` 用于完整项目的部署，而 `start-infra.sh` 则专注于基础设施的启动。

##### 2.2.1 `deploy.sh` 脚本命令

*   **`./deploy.sh start`**: 启动所有服务（包括基础服务和应用服务）。它会检查 Docker 环境、创建必要的目录、并启动 `docker-compose.yaml` 中定义的所有服务。
*   **`./deploy.sh stop`**: 停止所有正在运行的服务。
*   **`./deploy.sh restart`**: 重启所有服务。
*   **`./deploy.sh rebuild`**: 清理所有旧的容器、网络和卷，然后重新构建所有 Docker 镜像并启动所有服务。这对于进行干净的部署或应用代码更改非常有用。
*   **`./deploy.sh cleanup`**: 停止并删除所有容器、网络和卷。

##### 2.2.2 `start-infra.sh` 脚本命令

*   **`./start-infra.sh`**: 仅启动基础设施服务 (MySQL, Nacos, Prometheus)。这是开发过程中快速启动后端依赖的推荐方式。
    *   你可以通过设置 `BUILD_ENV` 环境变量来指定环境，例如 `BUILD_ENV=prod ./start-infra.sh`。

##### 2.2.3 部署流程

1.  **准备环境**:
    *   确保 Docker 和 Docker Compose 已安装。
    *   根据需要配置 `BUILD_ENV` 环境变量 (例如 `export BUILD_ENV=prod`)。
    *   确保 `config/dev.env` 和 `config/prod.env` 中的后端配置正确。
    *   确保 `teacher-system-web/.env.development` (或 `.env.production`) 中的前端配置正确，特别是 `VITE_API_TARGET`、`VITE_BASE_API` 和 `VITE_ATTACHMENT_BASE_URL`。

2.  **启动基础设施 (推荐)**:
    ```bash
    ./start-infra.sh
    ```
    验证基础设施服务（MySQL, Nacos, Prometheus）已正常运行。

3.  **构建和部署完整项目**:
    ```bash
    # 首次部署或有代码更改时，推荐使用 rebuild
    ./deploy.sh rebuild

    # 如果只是停止后重新启动，可以使用 start
    ./deploy.sh start
    ```

#### 2.3 数据库初始化 (`config/mysql-deploy/1-init.sql`)

*   `1-init.sql` 文件位于 `config/mysql-deploy/` 目录中。
*   在 `docker-compose.yaml` 中，MySQL 服务通过卷挂载将此文件映射到 `/docker-entrypoint-initdb.d/1-init.sql`。
*   这意味着 MySQL 容器在**首次启动时**会自动执行此脚本，创建所有数据库表并插入初始数据（角色和权限）。
*   **重要提示**: 如果你更改了 `1-init.sql` 文件，并且 MySQL 容器已经运行并初始化过数据库，那么仅仅重启容器不会再次执行 `1-init.sql`。你需要执行 `./deploy.sh cleanup` (这会删除 `mysql-data` 卷) 然后 `./deploy.sh rebuild` 来强制 MySQL 重新初始化数据库。

#### 2.4 前端附件 URL 配置

在前端代码中，`VITE_ATTACHMENT_BASE_URL` 现在会与其他环境变量组合，形成完整的附件 URL。

*   `VITE_API_TARGET`: 通常是后端服务的域名或 IP 地址，例如 `http://localhost:10001`。
*   `VITE_BASE_API`: API 的基础路径，例如 `/api/v1`。
*   `VITE_ATTACHMENT_BASE_URL`: 附件的相对路径，例如 `attachments/`。

完整的附件 URL 将被构建为 `${VITE_API_TARGET}${VITE_BASE_API}/${VITE_ATTACHMENT_BASE_URL}`。请确保这些环境变量在你的前端 `.env` 文件中正确配置，并且与你的后端服务配置相匹配。

---
