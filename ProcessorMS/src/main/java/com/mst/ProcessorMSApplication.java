package com.mst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ProcessorMSApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessorMSApplication.class, args);
    }

}
