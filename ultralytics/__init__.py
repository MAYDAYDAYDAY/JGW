# Ultralytics YOLO 🚀, AGPL-3.0 license

__version__ = "8.3.61"

import os

# Set ENV variables (place before imports)
if not os.environ.get("OMP_NUM_THREADS"):
    os.environ["OMP_NUM_THREADS"] = "1"  # default for reduced CPU utilization during training

# 仅直接加载 YOLO 子树；其余模型按需惰性加载（见 ultralytics.models 的 __getattr__）
from ultralytics.models.yolo.model import YOLO, YOLOWorld
from ultralytics.utils import ASSETS, SETTINGS
from ultralytics.utils.checks import check_yolo as checks
from ultralytics.utils.downloads import download

settings = SETTINGS


def __getattr__(name: str):
    if name == "NAS":
        from ultralytics.models.nas import NAS

        return NAS
    if name == "RTDETR":
        from ultralytics.models.rtdetr import RTDETR

        return RTDETR
    if name == "SAM":
        from ultralytics.models.sam import SAM

        return SAM
    if name == "FastSAM":
        from ultralytics.models.fastsam import FastSAM

        return FastSAM
    raise AttributeError(f"module {__name__!r} has no attribute {name!r}")


__all__ = (
    "__version__",
    "ASSETS",
    "YOLO",
    "YOLOWorld",
    "NAS",
    "SAM",
    "FastSAM",
    "RTDETR",
    "checks",
    "download",
    "settings",
)
