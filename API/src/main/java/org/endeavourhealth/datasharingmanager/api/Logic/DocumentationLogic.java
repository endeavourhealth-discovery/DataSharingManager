package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DocumentationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.datasharingmanager.api.DAL.DocumentationDAL;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class DocumentationLogic {

    public Response getAssociatedDocuments(String parentUuid, Short parentType) throws Exception {

        List<String> documentUuids = new SecurityMasterMappingDAL().getChildMappings(parentUuid, parentType, MapType.DOCUMENT.getMapType());
        List<DocumentationEntity> ret = new ArrayList<>();

        if (!documentUuids.isEmpty())
            ret = new DocumentationDAL().getDocumentsFromList(documentUuids);

        return Response
                .ok()
                .entity(ret)
                .build();

    }
}
