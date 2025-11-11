#!/bin/bash

# Teacher System Infrastructure Deployment Script
# 教师系统基础设施部署脚本

set -e

# 项目名称（用于隔离容器命名空间）
export COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME:-teacher-system}

# 默认环境
export BUILD_ENV=${BUILD_ENV:-dev}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to convert CRLF to LF for a given file
convert_crlf_to_lf() {
    local file_path="$1"
    if [ -f "$file_path" ]; then
        if grep -q $'
' "$file_path"; then
            print_info "Converting CRLF to LF in ${BLUE}$file_path${NC}..."
            sed -i 's/\r$//' "$file_path"
            print_info "Conversion complete."
        else
            print_info "${BLUE}$file_path${NC} already uses LF line endings. No conversion needed."
        fi
    fi
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    if ! command_exists docker; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command_exists docker-compose; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    print_info "Prerequisites check passed."
}

# Create necessary directories
create_directories() {
    print_info "Creating necessary directories for infrastructure..."
    
    mkdir -p config/mysql-deploy
    mkdir -p config/nacos-deploy
    mkdir -p config/prometheus-deploy
    
    print_info "Infrastructure directories created."
}

# Load environment variables from config file
load_env_vars() {
    ENV_FILE="./config/${BUILD_ENV}.env"
    if [ -f "${ENV_FILE}" ]; then
        print_info "Sourcing environment variables from: ${BLUE}${ENV_FILE}${NC} into shell environment."
        set -a # Automatically export all subsequent assignments
        . "${ENV_FILE}" # Source the env file
        set +a # Turn off automatic exporting
    else
        print_warn "Environment file ${ENV_FILE} not found. Ensure it exists or BUILD_ENV is set correctly."
        print_warn "Docker Compose will rely on default values or fallback to shell environment variables already set."
    fi
}

# Start infrastructure services
start_infrastructure() {
    print_info "Starting infrastructure services (MySQL, Nacos, Prometheus) for environment: ${BLUE}${BUILD_ENV}${NC}"
    # 停止并删除现有容器、网络和数据卷，确保每次都是全新启动
    docker compose down -v
    # 指定只启动基础设施服务并强制重新构建
    docker compose up --build -d mysql nacos prometheus
    print_info "Infrastructure services started."
}

# Main script execution
main() {
    check_prerequisites
    create_directories
    convert_crlf_to_lf "./config/mysql-deploy/init.sql" # Convert line endings for 1-init.sql
    convert_crlf_to_lf "./config/${BUILD_ENV}.env" # Convert line endings before sourcing
    load_env_vars # docker-compose 会自动处理 env_file，这里主要是为了日志提示

    start_infrastructure
    
    print_info "Infrastructure deployment completed successfully!"
    print_info "Environment: ${BLUE}${BUILD_ENV}${NC}"
    print_info "Access Nacos console at: http://localhost:8848/nacos"
    print_info "Access Prometheus console at: http://localhost:9090 (if enabled)"
}

main "$@"
