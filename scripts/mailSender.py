import smtplib
from email.mime.text import MIMEText
from email.header import Header
import argparse

# QQ邮箱的SMTP服务器地址和端口
smtp_server = 'smtp.qq.com'
smtp_port = 465

# 发件人QQ邮箱和授权码（需要在QQ邮箱中开启SMTP服务并获取授权码）
sender_email = "1141400667@qq.com"  # 替换为你的QQ邮箱
sender_name = "xiangrui"
sender_password = "fyihbocdmjbkfjdf"     # 替换为你的QQ邮箱授权码

def send_email(code, receiver_email):
    """
    发送邮件验证码
    :param code: 验证码
    :param receiver_email: 收件人邮箱
    """
    # 邮件内容
    subject = '您的验证码'  # 邮件主题
    body = f'您的验证码是：{code}，请妥善保管。'  # 邮件正文

    # 构造邮件
    message = MIMEText(body, 'plain', 'utf-8')
    message['From'] = Header(f"{sender_name} <{sender_email}>")
    message['To'] = Header(receiver_email, 'utf-8')  # 收件人
    message['Subject'] = Header(subject, 'utf-8')    # 主题

    try:
        # 连接到QQ邮箱的SMTP服务器
        server = smtplib.SMTP_SSL(smtp_server, smtp_port)
        server.login(sender_email, sender_password)  # 登录发件人邮箱
        server.sendmail(sender_email, [receiver_email], message.as_string())  # 发送邮件
        server.quit()
        print("邮件发送成功！")
        return 0  # 成功返回0
    except Exception as e:
        print(f"邮件发送失败：{e}")
        return 1  # 失败返回1

if __name__ == '__main__':
    # 解析命令行参数
    parser = argparse.ArgumentParser(description='发送邮箱验证码')
    parser.add_argument('code', type=str, default="123456", help='验证码')
    parser.add_argument('email', type=str, default="339938@whut.edu.cn",  help='目标邮箱')
    args = parser.parse_args()

    # 调用发送邮件函数
    exit_code = send_email(args.code, args.email)
    exit(exit_code)