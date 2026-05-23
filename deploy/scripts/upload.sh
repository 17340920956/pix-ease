#!/bin/bash
set -e

SERVER="101.42.17.176"
USER="ubuntu"
PASS="Yang1024@q"
REMOTE_BASE="/home/ubuntu/pix-ease"

echo "=== 上传 PixEase 项目到服务器 ==="
echo "服务器: $SERVER"
echo ""

REPO_ROOT="/Users/chen/codeRepository"

echo "1. 创建服务器目录结构..."
sshpass -p "$PASS" ssh -o StrictHostKeyChecking=no "$USER@$SERVER" "
    mkdir -p $REMOTE_BASE/{backend,web,admin,nginx,mysql/init,scripts,logs/prod,logs/qa}
"

echo "2. 上传 pix-ease 后端 (排除 target, deploy, .git)..."
rsync -avz --progress \
    --exclude 'target/' \
    --exclude '.git/' \
    --exclude '*.class' \
    -e "sshpass -p '$PASS' ssh -o StrictHostKeyChecking=no" \
    "$REPO_ROOT/pix-ease/" "$USER@$SERVER:$REMOTE_BASE/backend/"

echo "3. 上传 pix-ease-web 前端 (排除 node_modules, .git, .next)..."
rsync -avz --progress \
    --exclude 'node_modules/' \
    --exclude '.git/' \
    --exclude '.next/' \
    --exclude 'out/' \
    -e "sshpass -p '$PASS' ssh -o StrictHostKeyChecking=no" \
    "$REPO_ROOT/pix-ease-web/" "$USER@$SERVER:$REMOTE_BASE/web/"

echo "4. 上传 pix-ease-admin 前端 (排除 node_modules, .git, dist)..."
rsync -avz --progress \
    --exclude 'node_modules/' \
    --exclude '.git/' \
    --exclude 'dist/' \
    -e "sshpass -p '$PASS' ssh -o StrictHostKeyChecking=no" \
    "$REPO_ROOT/pix-ease-admin/" "$USER@$SERVER:$REMOTE_BASE/admin/"

echo "5. 上传部署配置..."
rsync -avz --progress \
    -e "sshpass -p '$PASS' ssh -o StrictHostKeyChecking=no" \
    "$REPO_ROOT/pix-ease/deploy/.env" \
    "$REPO_ROOT/pix-ease/deploy/nginx/nginx.conf" \
    "$REPO_ROOT/pix-ease/deploy/mysql/init/01-schema.sql" \
    "$REPO_ROOT/pix-ease/deploy/scripts/" \
    "$USER@$SERVER:$REMOTE_BASE/"

echo ""
echo "=== 上传完成 ==="
echo "服务器目录: $REMOTE_BASE"
echo ""
echo "下一步: SSH 登录服务器执行:"
echo "  cd $REMOTE_BASE && bash scripts/deploy.sh"