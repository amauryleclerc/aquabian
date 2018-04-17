package fr.aquabian.master.config;

import fr.aquabian.master.domain.ArDevice;
import fr.aquabian.master.domain.ArSensor;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfiguration {

    @Autowired
    public void configure(EventHandlingConfiguration configuration, Configurer configurer) {
        configuration.registerTrackingProcessor("Projection");

        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArDevice.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArSensor.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
        configurer.registerComponent(TokenStore.class, conf -> new InMemoryTokenStore());
    }


}