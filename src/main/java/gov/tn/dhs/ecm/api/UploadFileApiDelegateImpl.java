package gov.tn.dhs.ecm.api;

import com.box.sdk.*;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import gov.tn.dhs.ecm.AppProperties;
import gov.tn.dhs.ecm.model.UploadFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class UploadFileApiDelegateImpl implements UploadFileApiDelegate {

    private static Logger logger = LoggerFactory.getLogger(UploadFileApiDelegateImpl.class);

    @Autowired
    private AppProperties appProperties;

    @Override
    public ResponseEntity<UploadFileResponse> uploadFilePost(
            MultipartFile file,
            String boxFolderId,
            String programId,
            String caseId,
            Object documentTypeMetadata,
            Object confidentialityMetadata
    ) {

        logger.info(file.getName() + "," + boxFolderId);

        try {

            BoxDeveloperEditionAPIConnection api = getBoxDeveloperEditionAPIConnection();

            BoxFolder parentFolder = null;
            try {
                parentFolder = new BoxFolder(api, boxFolderId);
                BoxFolder.Info info = parentFolder.getInfo();
                logger.info("Parent Folder with ID {} and name {} found", boxFolderId, info.getName());
            } catch (BoxAPIException e) {
                UploadFileResponse uploadFileResponse = new UploadFileResponse();
                uploadFileResponse.setStatus("Folder not found");
                return new ResponseEntity<>(uploadFileResponse, HttpStatus.BAD_REQUEST);
            }
            String fileId = "No File Created";
            BoxFile.Info newFileInfo = null;
            try (InputStream inputStream = file.getInputStream()) {
                String fileName = file.getOriginalFilename();
                try {
                    newFileInfo = parentFolder.uploadFile(inputStream, fileName);
                } catch (BoxAPIException e) {
                    UploadFileResponse uploadFileResponse = new UploadFileResponse();
                    uploadFileResponse.setStatus("File with the same name already exists");
                    return new ResponseEntity<>(uploadFileResponse, HttpStatus.CONFLICT);
                }
            }
            fileId = newFileInfo.getID();
            BoxFile boxFile = new BoxFile(api, fileId);
            final JsonObject jsonObject = new JsonObject();
            jsonObject.add("programId", programId);
            jsonObject.add("caseId", caseId);
            JsonArray associationList = new JsonArray();
            associationList.add(jsonObject);
            final JsonObject associationObj = new JsonObject();
            String associatonListStr = associationList.toString();
            associationObj.add("AssociationMetadata", associatonListStr);
            Metadata metadata = new Metadata(associationObj);
            boxFile.createMetadata(metadata);
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            uploadFileResponse.setStatus("File upload completed");
            uploadFileResponse.setFileId(fileId);
            return new ResponseEntity<>(uploadFileResponse, HttpStatus.OK);
        } catch (Exception ex) {
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            uploadFileResponse.setStatus(ex.getMessage());
            return new ResponseEntity<>(uploadFileResponse, HttpStatus.CONFLICT);
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
