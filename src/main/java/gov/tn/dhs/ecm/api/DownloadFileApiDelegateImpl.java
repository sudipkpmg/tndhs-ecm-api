package gov.tn.dhs.ecm.api;

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import gov.tn.dhs.ecm.AppProperties;
import gov.tn.dhs.ecm.model.FileDownloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class DownloadFileApiDelegateImpl implements DownloadFileApiDelegate {

    private static Logger logger = LoggerFactory.getLogger(DownloadFileApiDelegateImpl.class);

    @Autowired
    private AppProperties appProperties;

    @Override
    public ResponseEntity<Resource> downloadFilePost(FileDownloadRequest fileDownloadRequest) {

        logger.info(fileDownloadRequest.toString());

        try {
            BoxDeveloperEditionAPIConnection api = getBoxDeveloperEditionAPIConnection();

            String fileId = fileDownloadRequest.getFileId();
            BoxFile file = new BoxFile(api, fileId);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            file.download(outputStream);
            final byte[] bytes = outputStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentLength(bytes.length);
            return new ResponseEntity(inputStreamResource, headers, HttpStatus.OK);
        }
        catch (Exception ex) {
            return new ResponseEntity(null, HttpStatus.CONFLICT);
        }

    }

    private BoxDeveloperEditionAPIConnection getBoxDeveloperEditionAPIConnection() {
        String clientId = appProperties.getClientID();
        String clientSecret = appProperties.getClientSecret();
        String enterpriseID = appProperties.getEnterpriseID();
        String publicKeyID = appProperties.getPublicKeyID();
        String privateKey = appProperties.getPrivateKey();
        String passphrase = appProperties.getPassphrase();
        BoxConfig boxConfig = new BoxConfig(
                clientId,
                clientSecret,
                enterpriseID,
                publicKeyID,
                privateKey,
                passphrase
        );
        BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);
        api.asUser(appProperties.getDownloadOneUserID());
        return api;
    }


}


