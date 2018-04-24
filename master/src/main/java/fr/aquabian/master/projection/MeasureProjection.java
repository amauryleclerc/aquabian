package fr.aquabian.master.projection;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import fr.aquabian.api.domain.event.AquabianEvents;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@ProcessingGroup("Projection")
public class MeasureProjection {

    private Double value = null;

    private Timestamp date = null;

    @EventHandler
    public void handle(AquabianEvents.MeasureAddedEvent event) {
        if (date == null || Timestamps.compare(event.getDate(), date) > 0) {
            value = event.getValue();
            date = event.getDate();
        }
    }

    @GetMapping("/aquabian/measure")
    public Response getLastMeasure(){
        return new Response("Il fait "+value+" Degrées");
    }



    @PostMapping("/aquabian/measure")
    public Response getLastMeasurePost(){
        return new Response("Il fait "+value+" Degrées");
    }

    public class Response {
        private final String speech;
        private final String displayText;

        public Response(String speech, String displayText) {
            this.speech = speech;
            this.displayText = displayText;
        }

        public Response(String text) {
            this.speech = text;
            this.displayText = text;
        }

        public String getSpeech() {
            return speech;
        }

        public String getDisplayText() {
            return displayText;
        }
    }
}
