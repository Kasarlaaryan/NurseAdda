package com.nurseadda.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NurseAddaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NurseAddaApplication.class, args);
    }
}
