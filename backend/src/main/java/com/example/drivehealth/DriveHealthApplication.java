package com.example.drivehealth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DriveHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriveHealthApplication.class, args);
    }
}
