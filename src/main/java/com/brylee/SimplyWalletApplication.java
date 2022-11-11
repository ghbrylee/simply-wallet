package com.brylee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class SimplyWalletApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SimplyWalletApplication.class);
        application.addListeners(new ApplicationPidFileWriter());
        application.run(args);
    }
}