package com.example.smartfactoryservice.Utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);

    private static JavaMailSender mailSender;
    private static String from;

    public EmailUtil(JavaMailSender mailSender,
                     @Value("${spring.mail.username}") String from) {
        EmailUtil.mailSender = mailSender;
        EmailUtil.from = from;
    }

    /**
     * 发送纯文本邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 正文
     */
    public static void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("纯文本邮件发送成功 -> {}", to);
        } catch (Exception e) {
            log.error("纯文本邮件发送失败 -> {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * 发送 HTML 邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param html    HTML 正文
     */
    public static void sendHtmlMail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("HTML 邮件发送成功 -> {}", to);
        } catch (MessagingException e) {
            log.error("HTML 邮件发送失败 -> {}: {}", to, e.getMessage(), e);
        }
    }
}
