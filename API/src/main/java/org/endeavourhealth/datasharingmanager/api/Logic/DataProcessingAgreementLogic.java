package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.DataProcessingAgreementDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.MasterMappingDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.PurposeDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDPA;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDocumentation;
import org.endeavourhealth.core.database.dal.usermanager.caching.*;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.*;
import org.endeavourhealth.core.database.rdbms.usermanager.models.UserRegionEntity;
import org.endeavourhealth.datasharingmanager.api.DAL.*;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataProcessingAgreementLogic {
    private static MasterMappingDalI masterMappingRepository = DalProvider.factoryDSMMasterMappingDal();
    private static PurposeDalI purposeRepository = DalProvider.factoryDSMPurposeDal();
    private static DataProcessingAgreementDalI dpaRepository = DalProvider.factoryDSMDataProcessingAgreementDal();

    public Response getDPA(String uuid, String searchData, String userId) throws Exception {

        if (uuid == null && searchData == null && userId == null) {
            return getDPAList();
        } else if (userId != null) {
            return getDPAListFilterOnRegion(userId);
        } else if (uuid != null){
            return getSingleDPA(uuid);
        } else {
            return search(searchData);
        }
    }

    public Response postDPA(JsonDPA dpa, String userProjectID) throws Exception {

        if (dpa.getUuid() != null) {
            new DataProcessingAgreementDAL().updateDPA(dpa, userProjectID, false);
        } else {
            dpa.setUuid(UUID.randomUUID().toString());
            new DataProcessingAgreementDAL().saveDPA(dpa, userProjectID);
        }

        return Response
                .ok()
                .entity(dpa.getUuid())
                .build();
    }

    public Response updateMappings(JsonDPA dpa, String userProjectID) throws Exception {

        new DataProcessingAgreementDAL().updateDPA(dpa, userProjectID, true);

        return Response
                .ok()
                .entity(dpa.getUuid())
                .build();
    }

    private Response getDPAList() throws Exception {

        List<DataProcessingAgreementEntity> dpas = new DataProcessingAgreementDAL().getAllDPAs();

        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    public Response getRegionlessDPAList() throws Exception {

        List<DataProcessingAgreementEntity> dpas = new DataProcessingAgreementDAL().getAllDPAs();
        List<DataProcessingAgreementEntity> regionlessDpas = new ArrayList<>();
        for (DataProcessingAgreementEntity dpa : dpas) {
            List<String> regionUuids = masterMappingRepository.getParentMappings(dpa.getUuid(),
                    MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.REGION.getMapType());
            if (regionUuids.isEmpty()) {
                regionlessDpas.add(dpa);
            }
        }

        return Response
                .ok()
                .entity(regionlessDpas)
                .build();
    }

    private Response getDPAListFilterOnRegion(String userId) throws Exception {

        UserRegionEntity userRegion = UserCache.getUserRegion(userId);

        List<DataProcessingAgreementEntity> dpas = DataProcessingAgreementCache.getAllDPAsForAllChildRegions(userRegion.getRegionId());

        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    private Response getSingleDPA(String uuid) throws Exception {
        DataProcessingAgreementEntity dpaEntity = new DataProcessingAgreementCache().getDPADetails(uuid);

        return Response
                .ok()
                .entity(dpaEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataProcessingAgreementEntity> dpas = new DataProcessingAgreementDAL().search(searchData);

        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    public Response getLinkedCohorts(String dpaUuid) throws Exception {
        List<String> cohorts = masterMappingRepository.getChildMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.COHORT.getMapType());

        List<CohortEntity> ret = new ArrayList<>();

        if (!cohorts.isEmpty())
            ret = CohortCache.getCohortDetails(cohorts);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedDataSets(String dpaUuid) throws Exception {
        List<String> datasets = masterMappingRepository.getChildMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.DATASET.getMapType());

        List<DataSetEntity> ret = new ArrayList<>();

        if (!datasets.isEmpty())
            ret = DataSetCache.getDataSetDetails(datasets);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedRegions(String dsaUuid, String userId) throws Exception {

        List<String> regionUuids = masterMappingRepository.getParentMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.REGION.getMapType());

        if (userId != null) {
            regionUuids = new RegionLogic().filterRegionsForUser(regionUuids, userId);
        }

        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = RegionCache.getRegionDetails(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getPublishers(String dsaUuid) throws Exception {

        List<String> publisherUuids = masterMappingRepository.getChildMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());

        List<OrganisationEntity> ret = new ArrayList<>();

        if (!publisherUuids.isEmpty())
            ret = OrganisationCache.getOrganisationDetails(publisherUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getPurposes(String dsaUuid) throws Exception {
        List<String> purposeUuids = masterMappingRepository.getChildMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PURPOSE.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (!purposeUuids.isEmpty())
            ret = purposeRepository.getPurposesFromList(purposeUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getBenefits(String dsaUuid) throws Exception {

        List<String> benefitUuids = masterMappingRepository.getChildMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.BENEFIT.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (!benefitUuids.isEmpty())
            ret = purposeRepository.getPurposesFromList(benefitUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response checkOrganisationIsPartOfDPA(String odsCode, boolean countOnly) throws Exception {

        List<DataProcessingAgreementEntity> matchingDpa = dpaRepository.getDataProcessingAgreementsForOrganisation(odsCode);

        if (countOnly) {
            return Response
                    .ok()
                    .entity(matchingDpa.size())
                    .build();
        }

        return Response
                .ok()
                .entity(matchingDpa)
                .build();
    }/*

    /*public Response checkOrganisationAndSystemIsPartOfDPA(String odsCode, String systemName) throws Exception {

        List<DataProcessingAgreementEntity> matchingDpa = new SecurityDataProcessingAgreementDAL().getDataProcessingAgreementsForOrganisationAndSystemType(odsCode, systemName);

        return Response
                .ok()
                .entity(matchingDpa)
                .build();
    }*/

    public Response addDocument(String uuid, JsonDocumentation document, String userProjectID) throws Exception {

        new DataProcessingAgreementDAL().addDocument(uuid, document, userProjectID);

        return Response
                .ok()
                .entity(uuid)
                .build();
    }
}
