package org.application.common.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHandler {
    private final BCryptPasswordEncoder encoder;

    public PasswordHandler() {
        this.encoder = new BCryptPasswordEncoder();
    }

    public String encodePassword(String password) {
        return encoder.encode(password);
    }
}
