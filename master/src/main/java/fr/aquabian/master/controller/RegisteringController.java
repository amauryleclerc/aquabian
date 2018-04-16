package fr.aquabian.master.controller;

import com.aquabian.api.domain.command.AquabianCommands;
import com.aquabian.api.domain.event.RegisteringService;
import fr.aquabian.master.projection.IDeviceService;
import io.reactivex.Completable;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.callbacks.LoggingCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static fr.aquabian.api.AquabianConstants.REGISTERING_SERVICE_PATH;
import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

@RestController
public class RegisteringController {

    private final IDeviceService deviceService;
    private final CommandBus commandBus;

    @Autowired
    public RegisteringController(final IDeviceService deviceService, final CommandBus commandBus) {
        this.deviceService = deviceService;
        this.commandBus = commandBus;
    }

    @RequestMapping(method = RequestMethod.POST, value = REGISTERING_SERVICE_PATH)
    public Completable register(@RequestBody byte[] bytes) {
        return Completable.fromAction(() -> {
            RegisteringService.RegisteringRequest request = RegisteringService.RegisteringRequest.parseFrom(bytes);

            if (!deviceService.isExist(request.getName())) {
                AquabianCommands.CreateDeviceCommand createDeviceCommand = AquabianCommands.CreateDeviceCommand.newBuilder()
                        .setId(request.getName())//
                        .setName(request.getName())//
                        .build();
                commandBus.dispatch(asCommandMessage(createDeviceCommand), LoggingCallback.INSTANCE);
            }

            request.getSensorsList()//
                    .stream()//
                    .filter(s -> !deviceService.getSensors(request.getName()).contains(s))//
                    .map(s -> AquabianCommands.CreateSensorCommand.newBuilder()//
                            .setId(s)//
                            .setName(s)//
                            .setDevice(request.getName())//
                            .build()
                    ).forEach(c ->commandBus.dispatch(asCommandMessage(c), LoggingCallback.INSTANCE));
        });
    }
}
