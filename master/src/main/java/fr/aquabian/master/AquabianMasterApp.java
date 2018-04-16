package fr.aquabian.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class AquabianMasterApp {


    private static final Logger LOGGER = LoggerFactory.getLogger(AquabianMasterApp.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AquabianMasterApp.class, args);
    }


}
