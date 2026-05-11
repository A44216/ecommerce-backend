package com.ecommerce.backend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

@Service
public class EmailService {

    private static final String APPLICATION_NAME = "Ecommerce App";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.gmail.refresh-token}")
    private String refreshToken;

    private Gmail getGmailService() throws Exception {
        GoogleCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private void sendEmailViaGmailApi(String toEmail, String subject, String content) {
        try {
            // Cấu hình Session giả lập để tạo MimeMessage
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage email = new MimeMessage(session);

            // "me" là từ khóa đặc biệt của Google API, đại diện cho tài khoản sở hữu token
            email.setFrom(new InternetAddress("me"));
            email.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toEmail));
            email.setSubject(subject);
            email.setContent(content, "text/html; charset=utf-8");

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);

            com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
            message.setRaw(encodedEmail);

            Gmail service = getGmailService();
            service.users().messages().send("me", message).execute();
            System.out.println("Email sent successfully to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Gmail API Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        String content = "Xin chao,<br><br>Ma xac thuc (OTP) de dang ky tai khoan cua ban la: <b>" + otpCode
                + "</b><br>Ma nay se het han sau 5 phut. Vui long khong chia se ma nay cho bat ky ai.<br><br>Tran trong.";
        sendEmailViaGmailApi(toEmail, "Ma xac thuc dang ky tai khoan", content);
    }

    @Async
    public void sendForgotPasswordEmail(String toEmail, String otpCode) {
        String content = "Xin chao,<br><br>Chung toi nhan duoc yeu cau dat lai mat khau. Ma xac thuc (OTP) cua ban la: <b>" + otpCode
                + "</b><br>Ma nay se het han sau 5 phut.<br><br>Neu ban khong yeu cau, vui long bo qua email nay.";
        sendEmailViaGmailApi(toEmail, "Yeu cau Dat lai mat khau", content);
    }

    @Async
    public void sendUnlinkEmailOtp(String toEmail, String otpCode) {
        String content = "Xin chao,<br><br>Ma xac thuc (OTP) de <b>HUY LIEN KET</b> Email nay la: <b>" + otpCode
                + "</b><br>Ma nay se het han sau 5 phut.";
        sendEmailViaGmailApi(toEmail, "Ma xac nhan huy lien ket Email", content);
    }

    @Async
    public void sendVerifyNewEmailOtp(String toEmail, String otpCode) {
        String content = "Xin chao,<br><br>Ma xac thuc (OTP) de xac nhan viec lien ket dia chi Email moi la: <b>" + otpCode
                + "</b><br>Ma nay se het han sau 5 phut.";
        sendEmailViaGmailApi(toEmail, "Ma xac nhan lien ket Email moi", content);
    }
}