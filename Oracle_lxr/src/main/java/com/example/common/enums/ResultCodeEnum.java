package com.example.common.enums;

public enum ResultCodeEnum {
    SUCCESS("200", "成功"),

    PARAM_ERROR("400", "参数异常"),
    TOKEN_INVALID_ERROR("401", "无效的token"),
    TOKEN_CHECK_ERROR("401", "token验证失败，请重新登录"),
    TOKEN_CHECK_ERROR2("402", "token解析失败，请重新登录"),
    PARAM_LOST_ERROR("4001", "参数缺失"),
    USER_NOT_EXIST("4002", "用户不存在"),

    SYSTEM_ERROR("500", "系统异常"),
    USER_EXIST_ERROR("5001", "用户名已存在"),
    USER_NOT_LOGIN("5002", "用户未登录"),
    USER_ACCOUNT_ERROR("5003", "账号或密码错误"),
    USER_NOT_EXIST_ERROR("5004", "用户不存在"),
    PARAM_PASSWORD_ERROR("5005", "原密码输入错误"),
    SEND_MAIL_ERROR("5006", "邮件发送失败"),
    BIGMODEL_ERROR("5008", "大模型错误"),
    CHAR_TABLE_NOT_FOUND("5009", "字不存在"),
    VALIDATE_CODE_ERROR("5007", "验证码错误");

    public String code;
    public String msg;

    ResultCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
