package com.castlelecs.booking.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {
    private boolean enabled;
    private String username;
    private String password;
    private String role;
}

