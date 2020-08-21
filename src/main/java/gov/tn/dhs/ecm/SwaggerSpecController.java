package gov.tn.dhs.ecm;

import gov.tn.dhs.ecm.api.SearchApiDelegateImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
public class SwaggerSpecController {

    private static Logger logger = LoggerFactory.getLogger(SwaggerSpecController.class);

    @RequestMapping(value = "/api-docs", method = RequestMethod.GET)
    public String showDocs() {
        String data = "Doc not found";
        Resource resource = new ClassPathResource("classpath:data.txt");
        try {
            InputStream inputStream = resource.getInputStream();
            byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
            data = new String(bdata, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return data;
    }

}
