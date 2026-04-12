#!/usr/bin/env python3
"""
钢铁缺陷检测系统打包脚本 - 跨平台版本
支持 Windows, macOS 和 Linux

Windows 提示：若项目路径含非 ASCII 字符（如中文目录），请勿使用
`conda run -n 环境名 python build.py`（conda 打印日志时可能触发 GBK 编码错误）。
请在对应 conda 环境中直接执行：`python build.py`。
"""
import os
import platform
import shutil
import subprocess
import sys
import importlib
from pathlib import Path

def clean_build_files():
    """清理之前的构建文件"""
    print("清理旧的构建文件...")
    if os.path.exists("dist"):
        shutil.rmtree("dist")
    if os.path.exists("build"):
        shutil.rmtree("build")
    if os.path.exists("steel_defect.spec"):
        os.remove("steel_defect.spec")

def get_platform_specific_options():
    """获取平台特定的打包选项"""
    system = platform.system()
    
    root = Path(__file__).resolve().parent
    rth = root / "rth_torch_dll.py"
    # 基本选项，所有平台通用（入口为桌面 GUI，非仅 API 的 main.py）
    options = [
        sys.executable, "-m", "PyInstaller",
        "--name", "steel_defect",
        "--onedir",
        "--clean",
        "run_gui.py",
    ]
    if system == "Windows" and rth.is_file():
        options.extend(["--runtime-hook", str(rth)])
    
    # 添加资源文件，注意分隔符在不同平台的区别
    if system == "Windows":
        separator = ";"
        # UPX 压缩偶发破坏原生 DLL，导致 c10 等加载 1114
        options.extend(["--noconsole", "--noupx"])
        # 如果有图标文件，可以添加
        # if os.path.exists("resources/icons/app.ico"):
        #     options.extend(["--icon", "resources/icons/app.ico"])
    else:  # macOS 和 Linux
        separator = ":"
        # macOS特定选项
        if system == "Darwin":
            options.extend(["--windowed"])  # macOS上使用独立窗口
            # 如果有图标文件，可以添加
            # if os.path.exists("resources/icons/app.icns"):
            #     options.extend(["--icon", "resources/icons/app.icns"])
    
    # 添加资源目录，使用平台特定的分隔符
    resources = [
        f"resources/model{separator}resources/model",
        f"resources/sql{separator}resources/sql",
        f"resources/report{separator}resources/report"
    ]
    
    for resource in resources:
        options.extend(["--add-data", resource])

    # 动态资源较多的依赖，显式收集以免运行时缺文件
    for pkg in ("qfluentwidgets", "ultralytics", "timm", "albumentations"):
        options.extend(["--collect-all", pkg])

    return options

def create_empty_folders():
    """创建必要的空目录"""
    os.makedirs("resources/report", exist_ok=True)
    
    # 检查模型目录
    if not os.path.exists("resources/model"):
        os.makedirs("resources/model", exist_ok=True)
        print("警告: resources/model 目录为空，请确保将模型文件放入此目录")


def dedupe_openmp_under_internal(internal_root: str) -> int:
    """
    删除 _internal 内多余的 libiomp5md.dll。多份 Intel OpenMP 同时映射易导致
    加载 torch\\lib\\c10.dll 时出现 WinError 1114。
    保留 torch\\lib 下的副本，尽量删 numpy.libs / scipy.libs 等中的重复。
    """
    if not os.path.isdir(internal_root):
        return 0
    target = "libiomp5md.dll"
    hits: list[str] = []
    for root, _, files in os.walk(internal_root):
        for f in files:
            if f.lower() == target:
                hits.append(os.path.join(root, f))
    if len(hits) <= 1:
        return 0

    def rank(path: str) -> tuple:
        pl = path.replace("\\", "/").lower()
        if "/torch/lib/" in pl:
            return (0, path)
        if "/numpy.libs/" in pl:
            return (1, path)
        if "/scipy.libs/" in pl:
            return (2, path)
        return (3, path)

    hits.sort(key=rank)
    removed = 0
    for p in hits[1:]:
        try:
            os.remove(p)
            removed += 1
            print(f"dedupe_openmp: removed {p}")
        except OSError as e:
            print(f"dedupe_openmp: skip {p}: {e}")
    print(f"dedupe_openmp: kept {hits[0]}, removed {removed} duplicate(s)")
    return removed


def dedupe_llvm_openmp_under_internal(internal_root: str) -> int:
    """删除多余的 libomp140.x86_64.dll（LLVM OpenMP），保留 torch\\lib 下副本。"""
    if not os.path.isdir(internal_root):
        return 0
    target = "libomp140.x86_64.dll"
    hits: list[str] = []
    for root, _, files in os.walk(internal_root):
        for f in files:
            if f.lower() == target.lower():
                hits.append(os.path.join(root, f))
    if len(hits) <= 1:
        return 0

    def rank(path: str) -> tuple:
        pl = path.replace("\\", "/").lower()
        if "/torch/lib/" in pl:
            return (0, path)
        return (1, path)

    hits.sort(key=rank)
    removed = 0
    for p in hits[1:]:
        try:
            os.remove(p)
            removed += 1
            print(f"dedupe_llvm_openmp: removed {p}")
        except OSError as e:
            print(f"dedupe_llvm_openmp: skip {p}: {e}")
    print(f"dedupe_llvm_openmp: kept {hits[0]}, removed {removed} duplicate(s)")
    return removed


def copy_torch_openmp_bootstrap(output_path: str) -> None:
    """
    将 torch\\lib 内 Intel/LLVM OpenMP 相关 dll 复制到 exe 同级目录。
    Windows 会优先从应用程序目录解析依赖，可缓解 c10 间接依赖加载失败 (1114)。
    """
    torch_lib = os.path.join(output_path, "_internal", "torch", "lib")
    if not os.path.isdir(torch_lib):
        return
    for name in os.listdir(torch_lib):
        ln = name.lower()
        if not ln.endswith(".dll"):
            continue
        if ln.startswith("libiomp") or ln.startswith("libomp") or ln == "libgcc_s_seh-1.dll" or ln.startswith("libwinpthread"):
            src = os.path.join(torch_lib, name)
            dst = os.path.join(output_path, name)
            try:
                shutil.copy2(src, dst)
                print(f"copy_torch_bootstrap: {name} -> exe dir")
            except OSError as e:
                print(f"copy_torch_bootstrap: skip {name}: {e}")


def main():
    """主函数：执行打包过程"""
    try:
        # 检查PyInstaller是否已安装 - 使用importlib而不是pip show
        try:
            importlib.import_module('PyInstaller')
            print("PyInstaller 已安装")
        except ImportError:
            print("正在安装 PyInstaller...")
            subprocess.run([sys.executable, "-m", "pip", "install", "PyInstaller"], check=True)
            try:
                importlib.import_module('PyInstaller')
                print("PyInstaller 安装成功")
            except ImportError:
                print("PyInstaller 安装失败，请手动运行: pip install PyInstaller")
                return 1
        
        # 清理旧的构建文件
        clean_build_files()
        
        # 创建必要的目录
        create_empty_folders()
        
        # 获取平台特定的打包选项
        options = get_platform_specific_options()
        
        # 执行打包命令
        system = platform.system()
        print(f"开始打包钢铁缺陷检测系统... 平台: {system}")
        print(f"命令: {' '.join(options)}")
        
        subprocess.run(options, check=True)

        # 打包完成
        if system == "Windows":
            output_path = "dist\\steel_defect"
        else:
            output_path = "dist/steel_defect"

        if system == "Windows":
            _internal = os.path.join(output_path, "_internal")
            dedupe_openmp_under_internal(_internal)
            dedupe_llvm_openmp_under_internal(_internal)
            copy_torch_openmp_bootstrap(output_path)

        print(f"\n打包完成！应用程序位于: {output_path}")
        zip_base = os.path.join("dist", "steel_defect_portable")
        if os.path.exists(zip_base + ".zip"):
            os.remove(zip_base + ".zip")
        archive_path = shutil.make_archive(
            zip_base, "zip", root_dir="dist", base_dir="steel_defect"
        )
        print(f"已生成分发压缩包: {archive_path}")
        print("解压 ZIP 到任意英文路径下，双击 steel_defect.exe 运行（建议路径不含中文）。")

        if system == "Windows":
            # 使用 ASCII 文件名，避免部分控制台/工具链下中文名乱码
            launcher = os.path.join(output_path, "Run_SteelDefect.bat")
            bat = (
                "@echo off\r\n"
                "cd /d \"%~dp0\"\r\n"
                "set \"PATH=%~dp0;%~dp0_internal\\torch\\lib;%~dp0_internal;%~dp0_internal\\numpy.libs;%~dp0_internal\\scipy.libs;%PATH%\"\r\n"
                "set KMP_DUPLICATE_LIB_OK=TRUE\r\n"
                "set MKL_THREADING_LAYER=GNU\r\n"
                "\"%~dp0steel_defect.exe\"\r\n"
            )
            with open(launcher, "w", encoding="ascii", errors="replace") as f:
                f.write(bat)
            print(f"已写入备用启动脚本: {launcher}")

    except Exception as e:
        print(f"打包过程中出错: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    sys.exit(main())