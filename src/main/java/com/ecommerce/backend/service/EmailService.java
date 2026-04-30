package com.ecommerce.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác thực đăng ký tài khoản");
        message.setText("Xin chào,\n\nMã xác thực (OTP) để đăng ký tài khoản của bạn là: " + otpCode
                + "\nMã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\nTrân trọng,\nĐội ngũ hỗ trợ.");

        mailSender.send(message);
    }
    @Async
    public void sendForgotPasswordEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Yêu cầu Đặt lại mật khẩu - Ứng dụng Thương mại điện tử");
        message.setText("Xin chào,\n\n"
                + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n"
                + "Mã xác thực (OTP) của bạn là: " + otpCode + "\n\n"
                + "Mã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Nếu bạn không yêu cầu đổi mật khẩu, vui lòng bỏ qua email này.\n\n"
                + "Trân trọng,\nĐội ngũ hỗ trợ.");

        mailSender.send(message);
    }

    @Async
    public void sendUnlinkEmailOtp(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác nhận hủy liên kết Email - Ứng dụng Thương mại điện tử");
        message.setText("Xin chào,\n\n"
                + "Chúng tôi nhận được yêu cầu thay đổi địa chỉ Email từ tài khoản của bạn.\n"
                + "Mã xác thực (OTP) để HỦY LIÊN KẾT Email này là: " + otpCode + "\n\n"
                + "Mã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Nếu bạn không thực hiện yêu cầu này, tài khoản của bạn có thể đang gặp rủi ro. Vui lòng đăng nhập và đổi mật khẩu ngay lập tức.\n\n"
                + "Trân trọng,\nĐội ngũ hỗ trợ.");

        mailSender.send(message);
    }

    @Async
    public void sendVerifyNewEmailOtp(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác nhận liên kết Email mới - Ứng dụng Thương mại điện tử");
        message.setText("Xin chào,\n\n"
                + "Mã xác thực (OTP) để xác nhận việc liên kết địa chỉ Email này với tài khoản của bạn là: " + otpCode + "\n\n"
                + "Mã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Trân trọng,\nĐội ngũ hỗ trợ.");

        mailSender.send(message);
    }

}