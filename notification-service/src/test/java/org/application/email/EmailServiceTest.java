package org.application.email;

import org.application.email.entity.EmailStructure;
import org.application.email.service.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    private final static String fromMail = "someEmail@yandex.ru";
    private final static String someEmail = "someEmail@yandex.ru";

    @Mock
    private JavaMailSender javaMailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void initialize() {
        emailService = new EmailServiceImpl(javaMailSender, fromMail);
    }

    @Test
    @DisplayName("Проверка метода отправки email")
    void shouldSendEmail() {
        EmailStructure emailStructure = new EmailStructure("subject", "message");

        emailService.sendMail(someEmail, emailStructure);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromMail);
        simpleMailMessage.setSubject(emailStructure.getSubject());
        simpleMailMessage.setText(emailStructure.getMessage());
        simpleMailMessage.setTo(someEmail);

        verify(javaMailSender).send(simpleMailMessage);
    }
}