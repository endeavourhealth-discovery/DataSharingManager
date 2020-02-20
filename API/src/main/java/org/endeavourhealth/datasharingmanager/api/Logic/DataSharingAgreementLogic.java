package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDSA;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDocumentation;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.*;
import org.endeavourhealth.common.security.usermanagermodel.models.database.UserRegionEntity;
import org.endeavourhealth.datasharingmanager.api.DAL.DataSharingAgreementDAL;
import org.endeavourhealth.datasharingmanager.api.DAL.PurposeDAL;
import org.endeavourhealth.datasharingmanager.api.DAL.RegionDAL;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataSharingAgreementLogic {


    public Response getDSAs(String uuid, String searchData, String userId) throws Exception {

        if (uuid == null && searchData == null && userId == null) {
            return getDSAList();
        } else if (userId != null) {
            return getDSAListFilterOnRegion(userId);
        } else if (uuid != null){
            return getSingleDSA(uuid);
        } else {
            return search(searchData);
        }
    }

    private Response getDSAListFilterOnRegion(String userId) throws Exception {

        UserRegionEntity userRegion = UserCache.getUserRegion(userId);

        List<DataSharingAgreementEntity> dpas = DataSharingAgreementCache.getAllDSAsForAllChildRegions(userRegion.getRegionId());

        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    public Response postDSA(JsonDSA dsa, String userProjectID) throws Exception {

        if (dsa.getUuid() != null) {
            new DataSharingAgreementDAL().updateDSA(dsa, userProjectID, false);
        } else {
            dsa.setUuid(UUID.randomUUID().toString());
            new DataSharingAgreementDAL().saveDSA(dsa, userProjectID);
        }

        return Response
                .ok(dsa.getUuid())
                .build();

    }

    public Response updateMappings(JsonDSA dsa, String userProjectID) throws Exception {

        new DataSharingAgreementDAL().updateDSA(dsa, userProjectID, true);

        return Response
                .ok()
                .entity(dsa.getUuid())
                .build();
    }

    private Response getDSAList() throws Exception {

        List<DataSharingAgreementEntity> dsas = new DataSharingAgreementDAL().getAllDSAs();

        return Response
                .ok()
                .entity(dsas)
                .build();
    }

    private Response getSingleDSA(String uuid) throws Exception {
        DataSharingAgreementEntity dsaEntity = DataSharingAgreementCache.getDSADetails(uuid);

        return Response
                .ok()
                .entity(dsaEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataSharingAgreementEntity> dsas = new DataSharingAgreementDAL().search(searchData);

        return Response
                .ok()
                .entity(dsas)
                .build();
    }

    public Response getLinkedRegions(String dsaUuid, String userId) throws Exception {

        List<String> regionUuids = new SecurityMasterMappingDAL().getParentMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.REGION.getMapType());

        if (userId != null) {
            regionUuids = new RegionLogic().filterRegionsForUser(regionUuids, userId);
        }

        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = new RegionDAL().getRegionsFromList(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getPublishers(String dsaUuid) throws Exception {

        List<String> publisherUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());

        List<OrganisationEntity> ret = new ArrayList<>();

        if (!publisherUuids.isEmpty())
            ret = OrganisationCache.getOrganisationDetails(publisherUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getSubscribers(String dsaUuid) throws Exception {

        List<String> subscriberUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.SUBSCRIBER.getMapType());

        List<OrganisationEntity> ret = new ArrayList<>();

        if (!subscriberUuids.isEmpty())
            ret = OrganisationCache.getOrganisationDetails(subscriberUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getPurposes(String dsaUuid) throws Exception {
        List<String> purposeUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PURPOSE.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (!purposeUuids.isEmpty())
            ret = new PurposeDAL().getPurposesFromList(purposeUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getBenefits(String dsaUuid) throws Exception {

        List<String> benefitUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.BENEFIT.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (!benefitUuids.isEmpty())
            ret = new PurposeDAL().getPurposesFromList(benefitUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getProjects(String dsaUuid) throws Exception {

        List<String> projectUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PROJECT.getMapType());

        List<ProjectEntity> ret = new ArrayList<>();

        if (!projectUuids.isEmpty())
            ret = ProjectCache.getProjectDetails(projectUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedCohorts(String dsaUUID) throws Exception {

        List<String> cohortUUIDs = new SecurityMasterMappingDAL().getChildMappings(dsaUUID, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.COHORT.getMapType());

        List<CohortEntity> ret = new ArrayList<>();

        if (!cohortUUIDs.isEmpty())
            ret = CohortCache.getCohortDetails(cohortUUIDs);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedDataSets(String dsaUUID) throws Exception {

        List<String> dataSetUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUUID, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.DATASET.getMapType());

        List<DatasetEntity> ret = new ArrayList<>();

        if (!dataSetUuids.isEmpty())
            ret = DataSetCache.getDataSetDetails(dataSetUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response addDocument(String uuid, JsonDocumentation document, String userProjectID) throws Exception {

        new DataSharingAgreementDAL().addDocument(uuid, document, userProjectID);

        return Response
                .ok()
                .entity(uuid)
                .build();
    }
}
