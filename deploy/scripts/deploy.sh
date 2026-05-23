#!/bin/bash
set -e

echo "=== PixEase 一键部署脚本 ==="
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$DEPLOY_DIR"

if ! command -v docker &> /dev/null; then
    echo "Docker 未安装，正在安装..."
    sudo apt-get update
    sudo apt-get install -y ca-certificates curl
    sudo install -m 0755 -d /etc/apt/keyrings
    sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    sudo chmod a+r /etc/apt/keyrings/docker.asc
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    sudo systemctl enable docker
    sudo systemctl start docker
    echo "Docker 安装完成"
fi

if ! groups ubuntu | grep -q docker; then
    sudo usermod -aG docker ubuntu
    echo "已将 ubuntu 用户加入 docker 组，请重新登录后生效"
fi

echo ""
echo "=== 构建并启动所有服务 ==="
docker compose build --no-cache
docker compose up -d

echo ""
echo "=== 等待服务启动 ==="
sleep 15

echo ""
echo "=== 服务状态 ==="
docker compose ps

echo ""
echo "=== 部署完成 ==="
echo "生产环境: https://yxbot.online/"
echo "QA 环境:   https://yxbot.online/qa/"
echo "管理后台:  https://yxbot.online/admin"
echo ""
echo "查看日志: docker compose logs -f [服务名]"
echo "Java 日志目录: ./logs/prod/ 和 ./logs/qa/"