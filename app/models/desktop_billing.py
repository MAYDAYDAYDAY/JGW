"""桌面端检测可选：从指定 API 用户扣费并写入 api_calls（与 HTTP 接口计费规则一致）。"""
from __future__ import annotations

from typing import List, Tuple

from PyQt6.QtCore import QSettings

from app.models.database.billing_dao import BillingDAO
from app.models.database.user_dao import UserDAO

_ORG = "SteelDefect"
_APP = "SteelDefectGUI"
_KEY_USER_ID = "billing/desktop_charge_user_id"


def get_desktop_charge_user_id() -> int:
    s = QSettings(_ORG, _APP)
    v = s.value(_KEY_USER_ID, 0)
    try:
        return int(v) if v is not None else 0
    except (TypeError, ValueError):
        return 0


def set_desktop_charge_user_id(user_id: int) -> None:
    QSettings(_ORG, _APP).setValue(_KEY_USER_ID, int(user_id))


def can_afford_desktop_batch(model_name: str, image_count: int) -> Tuple[bool, str]:
    """开始推理前检查余额是否够本次张数（未启用扣费则直接通过）。"""
    uid = get_desktop_charge_user_id()
    if uid <= 0 or image_count <= 0:
        return True, ""
    billing = BillingDAO()
    price = billing.get_model_price(model_name)
    if price is None:
        return True, ""
    need = float(price) * image_count
    user_dao = UserDAO()
    if not user_dao.check_balance(uid, need):
        u = user_dao.get_user_by_id(uid)
        bal = u["balance"] if u else 0.0
        return False, (
            f"已启用桌面扣费，当前账户余额不足：余额 {bal:.4f} 元，"
            f"本次需 {need:.4f} 元（{image_count} 张 × 单价）。请充值或关闭桌面扣费。"
        )
    return True, ""


def charge_desktop_after_saves(
    model_name: str, inference_result_ids: List[int]
) -> Tuple[bool, str]:
    """
    推理结果已成功写入 inference_results 后调用：按张数扣费并逐条写 api_calls。
    user_id<=0 或未配置单价则跳过，返回 (True, "").
    """
    uid = get_desktop_charge_user_id()
    if uid <= 0 or not inference_result_ids:
        return True, ""

    billing = BillingDAO()
    user_dao = UserDAO()
    price = billing.get_model_price(model_name)
    if price is None:
        return True, ""

    total = float(price) * len(inference_result_ids)
    user = user_dao.get_user_by_id(uid)
    if not user:
        return False, "桌面扣费账户不存在，已保存检测记录但未扣费"

    if not user_dao.check_balance(uid, total):
        return (
            False,
            f"余额不足：需 {total:.4f} 元，检测记录已保存但未扣费，请充值后补录或联系管理员。",
        )

    if not user_dao.deduct_balance(uid, total):
        return False, "扣款失败，检测记录已保存"

    token = user["token"]
    for iid in inference_result_ids:
        billing.record_api_call(
            user_id=uid,
            token=token,
            model_name=model_name,
            endpoint="/desktop",
            cost=float(price),
            image_count=1,
            status="success",
            inference_result_id=iid,
        )
    return True, ""
