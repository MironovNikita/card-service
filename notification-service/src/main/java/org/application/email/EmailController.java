package org.application.email;

import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.application.email.entity.EmailStructure;
import org.application.email.service.EmailService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Validated
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/send/{email}")
    public String sendMail(@PathVariable @Email String email, @RequestBody EmailStructure emailStructure) {
        emailService.sendMail(email, emailStructure);
        return String.format("Письмо было успешно отправлено на адрес %s", email);
    }
}
