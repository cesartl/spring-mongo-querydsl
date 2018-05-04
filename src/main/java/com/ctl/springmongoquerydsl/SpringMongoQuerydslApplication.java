package com.ctl.springmongoquerydsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class SpringMongoQuerydslApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMongoQuerydslApplication.class, args);
    }
}
