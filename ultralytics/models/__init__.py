# Ultralytics YOLO — 惰性导出，避免 import ultralytics 时先加载 FastSAM 等整条链，
# 在 Windows + PyInstaller 下可减轻 torch c10.dll 初始化顺序问题（本应用仅需 YOLO）。

__all__ = ("YOLO", "RTDETR", "SAM", "FastSAM", "NAS", "YOLOWorld")


def __getattr__(name: str):
    if name == "YOLO":
        from .yolo import YOLO

        return YOLO
    if name == "YOLOWorld":
        from .yolo import YOLOWorld

        return YOLOWorld
    if name == "NAS":
        from .nas import NAS

        return NAS
    if name == "RTDETR":
        from .rtdetr import RTDETR

        return RTDETR
    if name == "SAM":
        from .sam import SAM

        return SAM
    if name == "FastSAM":
        from .fastsam import FastSAM

        return FastSAM
    raise AttributeError(f"module {__name__!r} has no attribute {name!r}")
