package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.ExtractTechnicalDetailsDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.MasterMappingDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ExtractTechnicalDetailsEntity;

import java.util.List;

public class ExtractTechnicalDetailsLogic {
    private static ExtractTechnicalDetailsDalI extractRepository = DalProvider.factoryDSMExtractTechnicalDetailsDal();
    private static MasterMappingDalI masterMappingRepository = DalProvider.factoryDSMMasterMappingDal();

    public ExtractTechnicalDetailsEntity getAssociatedExtractTechnicalDetails(String parentUuid, Short parentType) throws Exception {

        List<String> detailsUuids = masterMappingRepository.getChildMappings(parentUuid, parentType, MapType.EXTRACTTECHNICALDETAILS.getMapType());
        ExtractTechnicalDetailsEntity ret = null;

        if (!detailsUuids.isEmpty()) {
            ret = extractRepository.getExtractTechnicalDetails(detailsUuids.get(0));
        }
        return ret;

    }
}
