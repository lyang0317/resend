package com.lenovo.msa.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 */
@SpringBootApplication(scanBasePackages = {"com.lenovo.msa.account"})
public class ResendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResendApplication.class, args);
    }


}
