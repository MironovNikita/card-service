package org.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.application.email.entity.EmailStructure;
import org.application.email.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
class EmailControllerTest {
    @MockBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    EmailController emailController;

    @Test
    @DisplayName("Проверка метода на отправку email")
    void shouldSendMail() throws Exception {
        String email = "testemail@test.com";
        EmailStructure emailStructure = new EmailStructure("subject", "message");

        mockMvc.perform(post("/email/send/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailStructure)))
                .andExpect(status().isOk())
                .andExpect(content().string("Письмо было успешно отправлено на адрес " + email));

        verify(emailService).sendMail(email, emailStructure);
    }

    @Test
    @DisplayName("Проверка метода на отправку email при некорректном email")
    void shouldNotSendMailIfEmailIsIncorrect() throws Exception {
        String email = "test.com";
        EmailStructure emailStructure = new EmailStructure("subject", "message");

        mockMvc.perform(post("/email/send/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailStructure)))
                .andExpect(status().isBadRequest());
    }
}