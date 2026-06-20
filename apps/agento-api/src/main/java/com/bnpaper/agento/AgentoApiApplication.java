package com.bnpaper.agento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AgentoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentoApiApplication.class, args);
    }
}
