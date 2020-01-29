package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ExtractTechnicalDetailsEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.datasharingmanager.api.DAL.ExtractTechnicalDetailsDAL;

import java.util.List;

public class ExtractTechnicalDetailsLogic {

    public ExtractTechnicalDetailsEntity getAssociatedExtractTechnicalDetails(String parentUuid, Short parentType) throws Exception {

        List<String> detailsUuids = new SecurityMasterMappingDAL().getChildMappings(parentUuid, parentType, MapType.EXTRACTTECHNICALDETAILS.getMapType());
        ExtractTechnicalDetailsEntity ret = null;

        if (!detailsUuids.isEmpty()) {
            ret = new ExtractTechnicalDetailsDAL().getExtractTechnicalDetails(detailsUuids.get(0));
        }
        return ret;

    }
}
