#!/bin/bash
# 编译打包 Vue3 前端
cd "$(dirname "$0")/.."

echo "=== 编译打包前端 ==="

# 安装依赖（如需要）
if [ ! -d "node_modules" ]; then
    echo "[INFO] 正在安装依赖..."
    npm install
fi

npm run build 2>&1 | tee logs/build-$(date +%Y%m%d-%H%M%S).log

if [ $? -eq 0 ]; then
    echo "[OK] 编译成功: dist/"
else
    echo "[FAIL] 编译失败，请查看日志"
    exit 1
fi
