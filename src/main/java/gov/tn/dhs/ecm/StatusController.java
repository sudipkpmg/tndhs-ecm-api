package gov.tn.dhs.ecm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    private static Logger logger = LoggerFactory.getLogger(StatusController.class);

    @RequestMapping(value = "/ecm-api/v1", method = RequestMethod.GET)
    public String index() {
        logger.info("Received status request");
        String msg = "TNDHS ECM API Service is running";
        return msg;
    }

}