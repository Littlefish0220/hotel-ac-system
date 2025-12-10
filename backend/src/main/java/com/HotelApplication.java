package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用启动入口
 * 替代原来的 Main.java 控制台程序
 */
@SpringBootApplication
public class HotelApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelApplication.class, args);
        System.out.println("=========================================");
        System.out.println("酒店空调管理系统已启动");
        System.out.println("API 地址: http://localhost:8080/api");
        System.out.println("=========================================");
    }
}
