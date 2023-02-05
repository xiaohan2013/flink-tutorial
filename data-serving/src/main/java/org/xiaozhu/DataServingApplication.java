package org.xiaozhu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "org.xiaozhu.mapper")
public class DataServingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataServingApplication.class, args);
    }

}
