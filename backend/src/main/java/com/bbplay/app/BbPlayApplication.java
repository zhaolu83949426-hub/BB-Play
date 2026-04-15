package com.bbplay.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.bbplay.app.mapper")
@SpringBootApplication
public class BbPlayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BbPlayApplication.class, args);
    }
}
