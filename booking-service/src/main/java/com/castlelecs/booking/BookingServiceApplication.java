package com.castlelecs.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.castlelecs.booking.configuration.AdminProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(AdminProperties.class)

public class BookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
