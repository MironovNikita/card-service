package org.application.email.service;

import org.application.email.entity.EmailStructure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromMail;

    public EmailServiceImpl(JavaMailSender javaMailSender,
                            @Value("${spring.mail.username}") String fromMail) {
        this.mailSender = javaMailSender;
        this.fromMail = fromMail;
    }

    @Override
    public void sendMail(String email, EmailStructure emailStructure) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromMail);
        simpleMailMessage.setSubject(emailStructure.getSubject());
        simpleMailMessage.setText(emailStructure.getMessage());
        simpleMailMessage.setTo(email);

        mailSender.send(simpleMailMessage);
    }
}
