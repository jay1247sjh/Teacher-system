docker network create teacher-system-net

docker-compose -f ./mysql-deploy/docker-compose.yml up -d

chmod +x ./nacos-deploy/init-db.sh

./nacos-deploy/init-db.sh 

chmod +x ./nacos-deploy/scripts/start.sh
chmod +x ./nacos-deploy/scripts/stop.sh

如果报错custom.env:行3: $'\r': 未找到命令
则在start.sh之前执行 sed -i 's/\r$//' ./nacos-deploy/nacos-config/custom.env

./nacos-deploy/scripts/start.sh