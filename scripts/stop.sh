#!/bin/bash
# 停止前端开发服务器
echo "=== 停止前端开发服务器 ==="
PID=$(ps aux | grep 'vite' | grep -v grep | awk '{print $1}')
if [ -n "$PID" ]; then
    kill $PID 2>/dev/null
    echo "[OK] 前端开发服务器已停止 (PID: $PID)"
else
    echo "[INFO] 未找到运行中的前端开发服务器"
fi
