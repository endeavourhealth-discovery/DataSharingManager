package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ExtractTechnicalDetailsEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.datasharingmanager.api.DAL.ExtractTechnicalDetailsDAL;

// import javax.ws.rs.core.Response;
// import java.util.ArrayList;
import java.util.List;

public class ExtractTechnicalDetailsLogic {

    public ExtractTechnicalDetailsEntity getAssociatedExtractTechnicalDetails(String parentUuid, Short parentType) throws Exception {

        List<String> detailsUuids = new SecurityMasterMappingDAL().getChildMappings(parentUuid, parentType, MapType.EXTRACTTECHNICALDETAILS.getMapType());
        ExtractTechnicalDetailsEntity ret = new ExtractTechnicalDetailsEntity();

        if (!detailsUuids.isEmpty())
            // System.out.println(detailsUuids.get(0));
            ret = new ExtractTechnicalDetailsDAL().getExtractTechnicalDetails(detailsUuids.get(0));
        // System.out.println(ret.toString());

        return ret;

    }
}
