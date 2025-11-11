#!/bin/bash

# Teacher System Docker Deployment Script
# 教师系统 Docker 部署脚本

set -e

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
    print_info "Creating necessary directories..."
    
    mkdir -p attachments/users
    mkdir -p attachments/table-data
    mkdir -p config/mysql-deploy
    mkdir -p config/nacos-deploy
    mkdir -p config/prometheus-deploy
    
    print_info "Directories created."
}

# Load environment variables from config file and export them to the shell environment
load_and_export_env_vars() {
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

# Start services
start_services() {
    print_info "Starting services with environment: ${BUILD_ENV}"
    docker-compose up -d
    print_info "All services started."
}

# Stop services
stop_services() {
    print_info "Stopping services..."
    docker-compose stop
    print_info "All services stopped."
}

# Restart services
restart_services() {
    print_info "Restarting services..."
    docker-compose restart
    print_info "All services restarted."
}

# Show service status
show_status() {
    print_info "Service status:"
    docker-compose ps
}

# Show logs
show_logs() {
    if [ -z "$1" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$1"
    fi
}

# Build services
build_services() {
    print_info "Building services for environment: ${BUILD_ENV}"
    BUILD_ENV=${BUILD_ENV} docker-compose build --no-cache
    print_info "Build completed."
}

# Clean up
cleanup() {
    print_warn "This will remove all containers and volumes. Are you sure? (y/N)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        print_info "Cleaning up..."
        docker-compose down -v
        print_info "Cleanup completed."
    else
        print_info "Cleanup cancelled."
    fi
}

# Show help
show_help() {
    cat << EOF
Teacher System Docker Deployment Script

Usage: $0 [COMMAND] [OPTIONS]

Environment Variables:
    BUILD_ENV    Set deployment environment (dev/prod, default: dev)
                 Example: BUILD_ENV=prod $0 start

Commands:
    start       Start all services
    stop        Stop all services
    restart     Restart all services
    status      Show service status
    logs        Show logs (add service name to show specific service logs)
    build       Build all services
    rebuild     Rebuild and restart all services
    clean       Stop and remove all containers and volumes
    help        Show this help message

Examples:
    $0 start                        # Start all services (dev environment)
    BUILD_ENV=prod $0 start         # Start all services (prod environment)
    $0 logs gateway                 # Show gateway service logs
    BUILD_ENV=prod $0 rebuild       # Rebuild and restart (prod environment)

Supported Environments:
    dev         Development environment (default)
    prod        Production environment

Notes:
    1. Docker Compose 将根据 BUILD_ENV 环境变量加载 `config/${BUILD_ENV}.env` 文件。
    2. `.env` 文件 (例如 `config/dev.env`, `config/prod.env`) 包含敏感信息，不应提交到版本控制。

EOF
}

# Main script
main() {
    case "${1:-start}" in
        start)
            check_prerequisites
            create_directories
            convert_crlf_to_lf "./config/${BUILD_ENV}.env" # Convert line endings before sourcing
            load_and_export_env_vars # Load and export environment variables
            print_info "Deploying with environment: ${BLUE}${BUILD_ENV}${NC}"
            start_services
            print_info "Deployment completed successfully!"
            print_info "Environment: ${BLUE}${BUILD_ENV}${NC}"
            print_info "Access the application at: http://localhost"
            print_info "Access Nacos console at: http://localhost:8848/nacos"
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs "$2"
            ;;
        build)
            check_prerequisites
            create_directories
            convert_crlf_to_lf "./config/${BUILD_ENV}.env" # Convert line endings before sourcing
            load_and_export_env_vars # Load and export environment variables
            build_services
            ;;
        rebuild)
            cleanup
            check_prerequisites
            create_directories
            convert_crlf_to_lf "./config/${BUILD_ENV}.env" # Convert line endings before sourcing
            load_and_export_env_vars # Load and export environment variables
            build_services
            start_services
            ;;
        clean)
            cleanup
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"

