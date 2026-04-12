#!/bin/bash
echo "正在启动钢铁缺陷检测系统..."
# macOS上应用程序可能是一个.app包
if [ -d "steel_defect.app" ]; then
    open steel_defect.app
else
    # 如果不是.app包，则直接执行二进制文件
    ./steel_defect
fi