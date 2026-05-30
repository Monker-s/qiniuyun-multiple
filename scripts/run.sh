#!/bin/bash
# 启动 Spring Boot 后端服务
cd "$(dirname "$0")/.."

echo "=== 启动多平台内容发布工具 - 后端服务 ==="

# 检查 MySQL 是否运行
mysqladmin ping -h localhost -u root -proot 2>/dev/null
if [ $? -ne 0 ]; then
    echo "[WARN] MySQL 未连接，请确保 MySQL 服务已启动"
fi

# 初始化数据库 (首次运行)
mysql -u root -proot < docs/schema.sql 2>/dev/null
if [ $? -eq 0 ]; then
    echo "[OK] 数据库初始化完成"
fi

# 启动 Spring Boot
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8" \
    2>&1 | tee logs/backend-$(date +%Y%m%d-%H%M%S).log
