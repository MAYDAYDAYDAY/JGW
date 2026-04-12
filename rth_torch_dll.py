# PyInstaller runtime hook：在解释器加载用户模块之前执行，早于 run_gui.py 里的 import。
# 解决 Windows 上 torch 加载 c10.dll 时出现 WinError 1114（多份 OpenMP / DLL 搜索顺序）。
import os
import sys


def _prepend_dll_path(p: str) -> None:
    if not p or not os.path.isdir(p):
        return
    os.environ["PATH"] = p + os.pathsep + os.environ.get("PATH", "")


if sys.platform == "win32" and getattr(sys, "frozen", False) and hasattr(sys, "_MEIPASS"):
    _base = sys._MEIPASS  # onedir 下为 .../steel_defect/_internal
    os.environ.setdefault("KMP_DUPLICATE_LIB_OK", "TRUE")
    os.environ.setdefault("MKL_THREADING_LAYER", "GNU")

    # exe 同级目录（打包脚本会拷入 libiomp 等），优先于 _internal
    _exe_dir = os.path.dirname(sys.executable)
    _prepend_dll_path(_exe_dir)

    # 让 torch 的 c10 及其依赖先于 numpy/scipy 的 OpenMP 被解析
    _torch_lib = os.path.join(_base, "torch", "lib")
    _prepend_dll_path(_torch_lib)
    _prepend_dll_path(os.path.join(_base, "numpy.libs"))
    _prepend_dll_path(os.path.join(_base, "scipy.libs"))
    _prepend_dll_path(_base)

    try:
        if os.path.isdir(_exe_dir):
            os.add_dll_directory(_exe_dir)
        os.add_dll_directory(_base)
        if os.path.isdir(_torch_lib):
            os.add_dll_directory(_torch_lib)
    except (AttributeError, OSError):
        pass
