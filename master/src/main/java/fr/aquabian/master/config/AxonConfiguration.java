package fr.aquabian.master.config;

import com.rabbitmq.client.Channel;
import fr.aquabian.master.domain.ArDevice;
import fr.aquabian.master.domain.ArSensor;
import org.axonframework.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.serialization.Serializer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableRabbit
public class AxonConfiguration {

    @Autowired
    public void configure(EventHandlingConfiguration configuration, Configurer configurer, SpringAMQPMessageSource myMessageSource) {
        configuration.registerTrackingProcessor("Projection");
        configurer.configureEventSerializer(c ->  serializer());
        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArDevice.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
        configurer.configureAggregate(
                AggregateConfigurer.defaultConfiguration(ArSensor.class)
                        .configureCommandTargetResolver(c -> new CommandTargetResolver()));
        //   configurer.registerComponent(TokenStore.class, conf -> new InMemoryTokenStore());
        configuration.registerSubscribingEventProcessor("GraphProjection", c -> myMessageSource);
    }

    @Bean
    public SpringAMQPMessageSource amqpMessageSource() {
        return new SpringAMQPMessageSource(serializer()) {
            @RabbitListener(bindings = @QueueBinding(
                    value = @Queue(durable = "true"),
                    exchange = @Exchange(value = "EventExchange"),
                    key = "fr.aquabian.api.domain.event")
            )
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                super.onMessage(message, channel);
            }
        };
    }

    @Primary
    @Bean
    public Serializer serializer() {
        return new ProtoSerializer();
    }


}