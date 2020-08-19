package gov.tn.dhs.ecm.api;

import com.box.sdk.*;
import com.eclipsesource.json.JsonObject;
import gov.tn.dhs.ecm.AppProperties;
import gov.tn.dhs.ecm.model.FolderCreationRequest;
import gov.tn.dhs.ecm.model.FolderCreationSuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CreateFolderApiDelegateImpl implements CreateFolderApiDelegate {

    private static Logger logger = LoggerFactory.getLogger(CreateFolderApiDelegateImpl.class);

    @Autowired
    AppProperties appProperties;

    @Override
    public ResponseEntity<FolderCreationSuccessResponse> createFolderPost(FolderCreationRequest folderCreationRequest) {

        logger.info(folderCreationRequest.toString());

        try {

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

            String parentFolderId = appProperties.getParentFolderID();
            BoxFolder parentFolder = new BoxFolder(api, parentFolderId);

            String folderID = "No folder created";

            String folderName = folderCreationRequest.getCitizenMetadata().getMpiId();

            BoxFolder.Info childFolderInfo = parentFolder.createFolder(folderName);
            folderID = childFolderInfo.getID();

            BoxFolder boxFolder = new BoxFolder(api, folderID);

            final JsonObject jsonObject = new JsonObject();
            jsonObject.add("FirstName", folderCreationRequest.getCitizenMetadata().getFirstName());
            jsonObject.add("LastName", folderCreationRequest.getCitizenMetadata().getLastName());
            jsonObject.add("DOB", folderCreationRequest.getCitizenMetadata().getDob().toString());
            jsonObject.add("last4ofssn", folderCreationRequest.getCitizenMetadata().getSsn4());

            Metadata metadata = new Metadata(jsonObject);
            boxFolder.createMetadata(metadata);

            FolderCreationSuccessResponse folderCreationSuccessResponse = new FolderCreationSuccessResponse();
            folderCreationSuccessResponse.setId(folderID);
            return new ResponseEntity<>(folderCreationSuccessResponse, HttpStatus.OK);

        } catch (BoxAPIException be) {
            if (be.getResponseCode() == 409) {
                FolderCreationSuccessResponse folderCreationSuccessResponse = new FolderCreationSuccessResponse();
                folderCreationSuccessResponse.setId("Folder already exists!");
                return new ResponseEntity<>(folderCreationSuccessResponse, HttpStatus.CONFLICT);
            }
            else {
                FolderCreationSuccessResponse folderCreationSuccessResponse = new FolderCreationSuccessResponse();
                folderCreationSuccessResponse.setId("Other Error!");
                return new ResponseEntity<>(folderCreationSuccessResponse, HttpStatus.CONFLICT);
            }
        }
        catch (Exception ex) {
            FolderCreationSuccessResponse folderCreationSuccessResponse = new FolderCreationSuccessResponse();
            folderCreationSuccessResponse.setId("Other Error!");
            return new ResponseEntity<>(folderCreationSuccessResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
