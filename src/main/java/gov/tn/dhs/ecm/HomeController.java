package gov.tn.dhs.ecm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private static Logger logger = LoggerFactory.getLogger(SwaggerSpecController.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        logger.info("Received status request");
        String msg = "TNDHS ecm-api service is running";
        return msg;
    }

}