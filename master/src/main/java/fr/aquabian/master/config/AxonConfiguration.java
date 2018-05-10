package fr.aquabian.master.config;

import com.rabbitmq.client.Channel;
import fr.aquabian.master.domain.ArDevice;
import fr.aquabian.master.domain.ArSensor;
import org.axonframework.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.axonframework.amqp.eventhandling.spring.SpringAMQPPublisher;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.upcasting.event.NoOpEventUpcaster;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
    public void configure(EventHandlingConfiguration configuration, Configurer configurer, SpringAMQPMessageSource myMessageSource) {
        configuration.registerTrackingProcessor("Projection");
        configurer.configureEventSerializer(c -> serializer());
        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArDevice.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArSensor.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
        //   configurer.registerComponent(TokenStore.class, conf -> new InMemoryTokenStore());
        configuration.registerSubscribingEventProcessor("GraphProjection", c -> myMessageSource);
    }

    @Autowired
    public void configure(EventBus eventBus) {
        SpringAMQPPublisher publisher = new SpringAMQPPublisher(eventBus);
        publisher.setSerializer(serializer());
        publisher.start();
    }

    @Bean
    public SpringAMQPMessageSource myMessageSource(Serializer serializer) {
        return new SpringAMQPMessageSource(serializer) {
            @RabbitListener(queues = "myQueue")
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                super.onMessage(message, channel);
            }
        };
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