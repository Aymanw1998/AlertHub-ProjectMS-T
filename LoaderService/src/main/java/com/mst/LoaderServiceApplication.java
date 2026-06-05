package com.mst;

import com.mst.integration.GitHubInte;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LoaderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoaderServiceApplication.class, args);
    }

//    @Bean
//    CommandLineRunner test(GitHubInte gitHubInte) {
//        return args -> {
//
//            System.out.println("===== GITHUB TEST =====");
//
//            gitHubInte.getRootContent()
//                    .forEach(item ->
//                            System.out.println(
//                                    item.getName() + " | " + item.getType()
//                            )
//                    );
//
//            System.out.println("===== END TEST =====");
//        };
//    }
}