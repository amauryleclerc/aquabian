package fr.aquabian.master.config;

import fr.aquabian.master.domain.ArDevice;
import fr.aquabian.master.domain.ArSensor;
import fr.aquabian.master.repo.TokenEntryRepo;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfiguration {

    @Autowired
    public void configure(EventHandlingConfiguration configuration, TokenEntryRepo tokenStoreRepo, Configurer configurer) {
        tokenStoreRepo.deleteAll();
        configuration.registerTrackingProcessor("Projection");

        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArDevice.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArSensor.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
    }


}