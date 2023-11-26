package ro.uaic.info.romandec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class RomanianManuscriptDecipherApplication {

    public static void main(String[] args) {
        SpringApplication.run(RomanianManuscriptDecipherApplication.class, args);
    }

}
