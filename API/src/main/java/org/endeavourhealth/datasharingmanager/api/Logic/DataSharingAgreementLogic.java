package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDSA;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDocumentation;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonPurpose;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.DataSharingAgreementCache;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.OrganisationCache;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.ProjectCache;
import org.endeavourhealth.datasharingmanager.api.DAL.*;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataSharingAgreementLogic {


    public Response getDSAs(String uuid, String searchData) throws Exception {
        if (uuid == null && searchData == null) {
            return getDSAList();
        } else if (uuid != null){
            return getSingleDSA(uuid);
        } else {
            return search(searchData);
        }
    }

    public Response postDSA(JsonDSA dsa) throws Exception {
        new PurposeDAL().deleteAllPurposes(dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());

        if (dsa.getUuid() != null) {
            new MasterMappingDAL().deleteAllMappings(dsa.getUuid());
            new DataSharingAgreementDAL().updateDSA(dsa);
        } else {
            dsa.setUuid(UUID.randomUUID().toString());
            new DataSharingAgreementDAL().saveDSA(dsa);
        }

        for (JsonDocumentation doc : dsa.getDocumentations()) {
            if (doc.getUuid() != null) {
                new DocumentationDAL().updateDocument(doc);
            } else {
                doc.setUuid(UUID.randomUUID().toString());
                new DocumentationDAL().saveDocument(doc);
            }
        }

        dsa.setPurposes(setUuidsAndSavePurpose(dsa.getPurposes()));
        dsa.setBenefits(setUuidsAndSavePurpose(dsa.getBenefits()));

        new MasterMappingDAL().saveDataSharingAgreementMappings(dsa);

        return Response
                .ok()
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

    public static List<JsonPurpose> setUuidsAndSavePurpose(List<JsonPurpose> purposes) throws Exception {
        for (JsonPurpose purpose : purposes) {
            if (purpose.getUuid() == null) {
                purpose.setUuid(UUID.randomUUID().toString());
            }
            new PurposeDAL().savePurpose(purpose);
        }

        return purposes;
    }

    public Response getLinkedDataFlows(String dsaUuid) throws Exception {

        List<String> dataFlowUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.DATAFLOW.getMapType());

        List<DataFlowEntity> ret = new ArrayList<>();

        if (!dataFlowUuids.isEmpty())
            ret = new DataFlowDAL().getDataFlowsFromList(dataFlowUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedRegions(String dsaUuid) throws Exception {

        List<String> regionUuids = new SecurityMasterMappingDAL().getParentMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.REGION.getMapType());

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
}
