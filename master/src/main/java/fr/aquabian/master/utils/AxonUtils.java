package fr.aquabian.master.utils;

import io.reactivex.Single;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

public class AxonUtils {

    public static <C, R> Single<CommandMessage<? extends C>> dispatch(CommandBus commandBus, C command) {
        return Single.create(obs -> {
            commandBus.<C, R>dispatch(asCommandMessage(command), new CommandCallback<C, R>() {

                @Override
                public void onSuccess(CommandMessage<? extends C> commandMessage, R result) {
                    obs.onSuccess(commandMessage);
                }

                @Override
                public void onFailure(CommandMessage<? extends C> commandMessage, Throwable cause) {
                    obs.onError(cause);
                }

            });

        });

    }
}
