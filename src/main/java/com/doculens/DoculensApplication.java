package com.doculens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DoculensApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoculensApplication.class, args);
    }

}
