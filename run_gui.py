import os
import sys

# Windows + PyInstaller + PyTorch：在 import numpy/torch 之前设置，降低 WinError 1114 概率。
# 根源上应使用 CPU 版 torch 打包（不含 CUDA/cuDNN DLL），见 requirements-windows-cpu.txt。
if sys.platform == "win32":
    os.environ.setdefault("KMP_DUPLICATE_LIB_OK", "TRUE")
    os.environ.setdefault("MKL_THREADING_LAYER", "GNU")
    if getattr(sys, "frozen", False) and hasattr(sys, "_MEIPASS"):
        _base = sys._MEIPASS  # PyInstaller onedir 下为 _internal 目录
        _exe_dir = os.path.dirname(sys.executable)
        _torch_lib = os.path.join(_base, "torch", "lib")
        # 依赖 PATH 顺序解析 c10.dll 的间接依赖（与 rth_torch_dll.py 一致）
        for _p in (
            _exe_dir,
            _torch_lib,
            os.path.join(_base, "numpy.libs"),
            os.path.join(_base, "scipy.libs"),
            _base,
        ):
            if os.path.isdir(_p):
                os.environ["PATH"] = _p + os.pathsep + os.environ.get("PATH", "")
        try:
            if os.path.isdir(_exe_dir):
                os.add_dll_directory(_exe_dir)
            os.add_dll_directory(_base)
            if os.path.isdir(_torch_lib):
                os.add_dll_directory(_torch_lib)
        except (AttributeError, OSError):
            pass

from PyQt6.QtWidgets import QApplication

from app.main_window import MainWindow

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = MainWindow()
    window.show()
    sys.exit(app.exec())