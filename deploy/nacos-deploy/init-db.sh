CONTAINER_NAME="teacher-system-mysql"
MYSQL_USER="root"
MYSQL_PASS="root"
MYSQL_DB="nacos_config"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="${SCRIPT_DIR}/sql/nacos.sql"

echo "👉 检查 MySQL 容器是否在运行..."
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
  echo "❌ 容器 ${CONTAINER_NAME} 未启动，请先执行：docker-compose up -d"
  exit 1
fi

echo "👉 创建数据库（如不存在）..."
docker exec -i ${CONTAINER_NAME} \
  mysql -u${MYSQL_USER} -p${MYSQL_PASS} \
  -e "CREATE DATABASE IF NOT EXISTS \`${MYSQL_DB}\` CHARACTER SET utf8 COLLATE utf8_bin;"

echo "👉 导入 Nacos 表结构..."
docker exec -i ${CONTAINER_NAME} \
  sh -c "mysql -u${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB}" < "$SQL_FILE"

if [ $? -eq 0 ]; then
    echo "✅ Nacos 数据库初始化完成"
else
    echo "❌ 初始化失败，请检查 docker-compose 和 MySQL 配置"
    exit 1
fi