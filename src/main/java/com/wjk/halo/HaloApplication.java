package com.wjk.halo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//@EnableJpaRepositories(basePackages = "com.wjk.halo.repository", repositoryBaseClass = BaseRepositoryImpl.class)
public class HaloApplication {

    public static void main(String[] args) {
        SpringApplication.run(HaloApplication.class, args);
    }

}
