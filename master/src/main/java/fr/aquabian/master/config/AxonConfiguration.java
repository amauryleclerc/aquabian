package fr.aquabian.master.config;

import fr.aquabian.master.domain.ArDevice;
import fr.aquabian.master.domain.ArSensor;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.serialization.upcasting.event.NoOpEventUpcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class AxonConfiguration {

    @Autowired
    public void configure(EventHandlingConfiguration configuration, Configurer configurer) {
        configuration.registerTrackingProcessor("Projection");
       configurer.configureEventSerializer(c -> serializer() );
        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArDevice.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArSensor.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
     //   configurer.registerComponent(TokenStore.class, conf -> new InMemoryTokenStore());
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public EventStorageEngine eventStorageEngine(DataSource dataSource) throws SQLException {

        EntityManagerProvider entityManagerProvider = new SimpleEntityManagerProvider(entityManager);
        return new JpaEventStorageEngine(serializer(), NoOpEventUpcaster.INSTANCE, dataSource, entityManagerProvider, NoTransactionManager.INSTANCE);
    }

    private Serializer serializer() {
        return new ProtoSerializer();
    }


}