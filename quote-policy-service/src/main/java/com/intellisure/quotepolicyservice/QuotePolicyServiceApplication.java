package com.intellisure.quotepolicyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class QuotePolicyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                QuotePolicyServiceApplication.class,
                args
        );
    }
}