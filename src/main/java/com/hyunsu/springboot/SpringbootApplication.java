package com.hyunsu.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringbootApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }

}
