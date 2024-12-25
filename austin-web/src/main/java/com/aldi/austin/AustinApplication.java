package com.aldi.austin;

import com.aldi.austin.common.constant.AustinConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author 3y
 */
@SpringBootApplication
@Slf4j
public class AustinApplication implements CommandLineRunner {

    @Value("${server.port}")
    private String serverPort;


    public static void main(String[] args) {
        SpringApplication.run(AustinApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info(AnsiOutput.toString(AustinConstant.PROJECT_BANNER, "\n", AnsiColor.GREEN, AustinConstant.PROJECT_NAME, AnsiColor.DEFAULT, AnsiStyle.FAINT));
        log.info("Austin start succeeded, Index >> http://127.0.0.1:{}/", serverPort);
        log.info("Austin start succeeded, Swagger Url >> http://127.0.0.1:{}/swagger-ui/index.html", serverPort);
    }
}
