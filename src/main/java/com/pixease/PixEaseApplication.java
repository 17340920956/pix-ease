package com.pixease;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PixEase应用启动类
 */
@SpringBootApplication
@MapperScan("com.pixease.mapper")
public class PixEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PixEaseApplication.class, args);
    }
}
