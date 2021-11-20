package com.gatech.bigdata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"com.gatech.bigdata"})
@MapperScan("com.gatech.bigdata.mybatisplus.mapper")
public class HBaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(HBaseApplication.class, args);
    }
}
