package fr.aquabian.master.controller;

import com.aquabian.api.domain.command.AquabianCommands;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.callbacks.LoggingCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static fr.aquabian.api.AquabianConstants.COMMAND_PATH;
import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

@RestController
public class CommandController {

    private final CommandBus commandBus;

    @Autowired
    public CommandController(final CommandBus commandBus) {
        this.commandBus = commandBus;
    }


    @RequestMapping(method = RequestMethod.POST, value = COMMAND_PATH)
    public Completable command(@RequestBody byte[] bytes) {
        return Single.fromCallable(() -> AquabianCommands.AquabianCommand.parseFrom(bytes))//
                .flatMapObservable(c -> Observable.fromIterable(c.getAllFields().values()))//
                .flatMapCompletable(c -> Completable.fromAction(() -> commandBus.dispatch(asCommandMessage(c), LoggingCallback.INSTANCE)));
    }

}