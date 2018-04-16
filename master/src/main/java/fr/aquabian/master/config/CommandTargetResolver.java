package fr.aquabian.master.config;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.VersionedAggregateIdentifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandTargetResolver implements org.axonframework.commandhandling.CommandTargetResolver {


    public static final String GET_ID = "getId";

    public CommandTargetResolver() {
    }

    @Override
    public VersionedAggregateIdentifier resolveTarget(CommandMessage<?> command) {

        try {
            Method method = command.getPayload().getClass().getMethod(GET_ID);
            Object id = method.invoke(command.getPayload());
            return new VersionedAggregateIdentifier(id.toString(), null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
