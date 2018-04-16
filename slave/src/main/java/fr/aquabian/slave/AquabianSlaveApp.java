package fr.aquabian.slave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AquabianSlaveApp {


    private static final Logger LOGGER = LoggerFactory.getLogger(AquabianSlaveApp.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AquabianSlaveApp.class, args);
    }


}
