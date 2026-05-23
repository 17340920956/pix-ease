#!/bin/bash
set -e

echo "=== PixEase 数据备份脚本 ==="
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_DIR="$DEPLOY_DIR/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/pix-ease-backup-$TIMESTAMP.tar.gz"

mkdir -p "$BACKUP_DIR"

echo "备份 MySQL 数据库..."
cd "$DEPLOY_DIR"
docker compose exec -T mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD:-pixEase2026!} --all-databases > "$BACKUP_DIR/db-dump-$TIMESTAMP.sql"

echo "备份配置文件..."
tar -czf "$BACKUP_FILE" \
    -C "$DEPLOY_DIR" \
    .env docker-compose.yml nginx/ mysql/init/ \
    "$BACKUP_DIR/db-dump-$TIMESTAMP.sql"

rm "$BACKUP_DIR/db-dump-$TIMESTAMP.sql"

echo "备份完成: $BACKUP_FILE"
echo ""

find "$BACKUP_DIR" -name "pix-ease-backup-*.tar.gz" -mtime +30 -delete
echo "已清理30天前的旧备份"