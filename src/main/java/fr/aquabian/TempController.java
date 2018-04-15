package fr.aquabian;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TempController {


    private static final Logger LOGGER = LoggerFactory.getLogger(TempController.class);

    private final TempService tempService;

    @Autowired
    public TempController(TempService tempService){
        this.tempService = tempService;

    }

    @GetMapping("/temp")
    public String getTemp(){
        final String temp = tempService.getTemp().map(Object::toString).orElse("Temp not found !");
        LOGGER.info("Temp : {}",temp);
        return temp;
    }
}
