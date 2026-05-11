package com.ecommerce.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private void sendEmailViaApi(String toEmail, String subject, String content) {
        try {
            String jsonBody = """
                {
                   "sender": {"email": "%s", "name": "Đội ngũ hỗ trợ"},
                   "to": [{"email": "%s"}],
                   "subject": "%s",
                   "htmlContent": "<html><body>%s</body></html>"
                }
                """.formatted(senderEmail, toEmail, subject, content.replace("\n", "<br>"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Gửi email thành công tới: " + toEmail);
            } else {
                System.err.println("Lỗi Brevo API: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống gửi mail: " + e.getMessage());
        }
    }

    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        String content = "Xin chào,<br><br>Mã xác thực (OTP) để đăng ký tài khoản của bạn là: <b>" + otpCode
                + "</b><br>Mã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.<br><br>Trân trọng.";
        sendEmailViaApi(toEmail, "Mã xác thực đăng ký tài khoản", content);
    }

    @Async
    public void sendForgotPasswordEmail(String toEmail, String otpCode) {
        String content = "Xin chào,<br><br>Chúng tôi nhận được yêu cầu đặt lại mật khẩu. Mã xác thực (OTP) của bạn là: <b>" + otpCode
                + "</b><br>Mã này sẽ hết hạn sau 5 phút.<br><br>Nếu bạn không yêu cầu, vui lòng bỏ qua email này.";
        sendEmailViaApi(toEmail, "Yêu cầu Đặt lại mật khẩu", content);
    }

    @Async
    public void sendUnlinkEmailOtp(String toEmail, String otpCode) {
        String content = "Xin chào,<br><br>Mã xác thực (OTP) để <b>HỦY LIÊN KẾT</b> Email này là: <b>" + otpCode
                + "</b><br>Mã này sẽ hết hạn sau 5 phút.<br><br>Nếu không phải bạn thực hiện, hãy đổi mật khẩu ngay.";
        sendEmailViaApi(toEmail, "Mã xác nhận hủy liên kết Email", content);
    }

    @Async
    public void sendVerifyNewEmailOtp(String toEmail, String otpCode) {
        String content = "Xin chào,<br><br>Mã xác thực (OTP) để xác nhận việc liên kết địa chỉ Email mới là: <b>" + otpCode
                + "</b><br>Mã này sẽ hết hạn sau 5 phút.";
        sendEmailViaApi(toEmail, "Mã xác nhận liên kết Email mới", content);
    }
}