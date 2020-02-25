package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.MasterMappingDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.usermanager.caching.DocumentationCache;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DocumentationEntity;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class DocumentationLogic {
    private static MasterMappingDalI masterMappingRepository = DalProvider.factoryDSMMasterMappingDal();

    public Response getAssociatedDocuments(String parentUuid, Short parentType) throws Exception {

        List<String> documentUuids = masterMappingRepository.getChildMappings(parentUuid, parentType, MapType.DOCUMENT.getMapType());
        List<DocumentationEntity> ret = new ArrayList<>();

        if (!documentUuids.isEmpty())
            ret = DocumentationCache.getDocumentDetails(documentUuids);

        return Response
                .ok()
                .entity(ret)
                .build();

    }
}
