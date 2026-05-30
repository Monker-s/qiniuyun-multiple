#!/bin/bash
# 启动 Vue3 前端开发服务器
cd "$(dirname "$0")/.."

echo "=== 启动多平台内容发布工具 - 前端开发服务器 ==="

# 安装依赖（如需要）
if [ ! -d "node_modules" ]; then
    echo "[INFO] 正在安装依赖..."
    npm install
fi

# 启动 Vite 开发服务器
npm run dev 2>&1 | tee logs/frontend-$(date +%Y%m%d-%H%M%S).log
