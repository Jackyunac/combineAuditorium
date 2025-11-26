package com.combine.auditorium;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.combine.auditorium.mapper")
public class CombineAuditoriumApplication {

    public static void main(String[] args) {
        // 强制设置 JVM 编码为 UTF-8，解决 Windows 下默认 GBK 导致的乱码问题
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        
        SpringApplication.run(CombineAuditoriumApplication.class, args);
    }

}
