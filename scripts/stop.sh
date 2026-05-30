#!/bin/bash
# 停止后端服务
echo "=== 停止后端服务 ==="
PID=$(ps aux | grep 'content-publish-server' | grep -v grep | awk '{print $1}')
if [ -n "$PID" ]; then
    kill $PID 2>/dev/null
    echo "[OK] 后端服务已停止 (PID: $PID)"
else
    echo "[INFO] 未找到运行中的后端服务"
fi
