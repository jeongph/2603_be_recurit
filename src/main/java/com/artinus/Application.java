package com.artinus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {
        "com.artinus.subscription",
        "com.artinus.history",
        "com.artinus.plugin",
        "com.artinus.common"
})
@ConfigurationPropertiesScan(basePackages = "com.artinus")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
