package com.mydrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class MyDriveApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyDriveApplication.class, args);
    }
}