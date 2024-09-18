package com.inkneko.nekowindow.user.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class UserServiceConfig {
    @Value("${spring.mail.username}")
    String emailFrom;
}
