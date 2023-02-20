package org.edu_sharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class EMailNotifierApplication {

    public static void main(String[] args) {
        SpringApplication.run(EMailNotifierApplication.class, args);
    }

}
