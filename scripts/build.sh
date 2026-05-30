#!/bin/bash
# 编译打包 Spring Boot 后端
cd "$(dirname "$0")/.."

echo "=== 编译打包后端服务 ==="
mvn clean package -DskipTests 2>&1 | tee logs/build-$(date +%Y%m%d-%H%M%S).log

if [ $? -eq 0 ]; then
    echo "[OK] 编译成功: target/content-publish-server-1.0.0.jar"
else
    echo "[FAIL] 编译失败，请查看日志"
    exit 1
fi
