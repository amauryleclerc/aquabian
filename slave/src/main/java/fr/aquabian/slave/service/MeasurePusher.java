package fr.aquabian.slave.service;

import fr.aquabian.api.domain.command.AquabianCommands;
import fr.aquabian.api.domain.event.RegisteringService;
import com.google.protobuf.util.Timestamps;
import fr.aquabian.api.AquabianConstants;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;


@Service
public class MeasurePusher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sensor.class);
    private final URI masterCommandUri;
    private final ISensorManager sensorManager;
    private final RestTemplate restTemp;
    private final String hostname;
    private final URI masterRegisteringUri;


    @Autowired
    public MeasurePusher(@Value("${master.uri}") String masterUri, ISensorManager sensorManager) {
        this.masterRegisteringUri = URI.create(masterUri + AquabianConstants.REGISTERING_SERVICE_PATH);
        this.masterCommandUri = URI.create(masterUri + AquabianConstants.COMMAND_PATH);
        this.sensorManager = sensorManager;
        this.restTemp = new RestTemplate();
        this.hostname = getHostName();

    }


    @PostConstruct
    private void init() {
        Observable.fromCallable(() -> {
            RegisteringService.RegisteringRequest registeringRequest = RegisteringService.RegisteringRequest.newBuilder()
                    .setName(hostname)//
                    .addAllSensors(sensorManager.getSensors().stream().map(ISensor::getSensorId).collect(Collectors.toSet()))//
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/octet-stream");
            HttpEntity<byte[]> entity = new HttpEntity<>(registeringRequest.toByteArray(), headers);
            return restTemp.exchange(masterRegisteringUri, HttpMethod.POST, entity, byte[].class);
        })//
                .concatWith(
                        Observable.fromIterable(sensorManager.getSensors())//
                                .subscribeOn(Schedulers.computation())//
                                .flatMap(s -> Observable.interval(10, TimeUnit.SECONDS)//
                                        .startWith(0L)//
                                        .map(d -> s.getMeasure())
                                        .filter(Optional::isPresent))
                                .map(Optional::get)//
                                .map(m -> AquabianCommands.AquabianCommand.newBuilder()//
                                        .setAddMeasureCommand(AquabianCommands.AddMeasureCommand.newBuilder()
                                                .setDate(Timestamps.fromMillis(m.getDate().toEpochMilli()))//
                                                .setId(m.getSensorId())//
                                                .setValue(m.getValue()))//
                                        .build())
                                .map(command -> {
                                            LOGGER.info("add measure {} for {}", command.getAddMeasureCommand().getValue(), command.getAddMeasureCommand().getId());
                                            HttpHeaders headers = new HttpHeaders();
                                            headers.set("Content-Type", "application/octet-stream");
                                            HttpEntity<byte[]> entity = new HttpEntity<>(command.toByteArray(), headers);
                                            return restTemp.exchange(masterCommandUri, HttpMethod.POST, entity, byte[].class);
                                        }
                                ))
                .flatMap(r -> {
                    if(r.getStatusCode().isError()){
                        return Observable.error(new RuntimeException(r.getStatusCode().getReasonPhrase()));
                    }
                    return Observable.just(r);
                })
                .doOnError(t -> LOGGER.error("Error :", t))//
                .retryWhen(obs -> obs.delay(3000, TimeUnit.MILLISECONDS))//
                .subscribe(m -> {
                    LOGGER.info("status : {}", m.getStatusCode());
                }, t -> LOGGER.error("Error", t));
        LockSupport.park();


    }


    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOGGER.error("Cannot found hostname");
            return UUID.randomUUID().toString();
        }
    }

}
