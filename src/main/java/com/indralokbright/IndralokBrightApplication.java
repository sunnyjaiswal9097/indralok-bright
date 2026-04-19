package com.indralokbright;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class IndralokBrightApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndralokBrightApplication.class, args);
        log.info("========================================");
        log.info("  Indralok Bright Application Started  ");
        log.info("  URL: http://localhost:8080            ");
        log.info("  Login: admin / admin123               ");
        log.info("========================================");
    }
}
