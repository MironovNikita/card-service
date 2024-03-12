package org.application.email.service;

import org.application.email.entity.EmailStructure;

public interface EmailService {
    void sendMail(String email, EmailStructure emailStructure);
}
