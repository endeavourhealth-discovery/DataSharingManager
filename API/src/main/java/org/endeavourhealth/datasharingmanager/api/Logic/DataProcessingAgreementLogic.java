package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityCohortDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityDatasetDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDPA;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDocumentation;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.OrganisationCache;
import org.endeavourhealth.datasharingmanager.api.DAL.*;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataProcessingAgreementLogic {

    public Response getDPA(String uuid, String searchData) throws Exception {

        if (uuid == null && searchData == null) {
            return getDPAList();
        } else if (uuid != null){
            return getSingleDPA(uuid);
        } else {
            return search(searchData);
        }
    }

    public Response postDPA(JsonDPA dpa) throws Exception {


        if (dpa.getUuid() != null) {
            new MasterMappingDAL().deleteAllMappings(dpa.getUuid());
            new DataProcessingAgreementDAL().updateDPA(dpa);
        } else {
            dpa.setUuid(UUID.randomUUID().toString());
            new DataProcessingAgreementDAL().saveDPA(dpa);
        }

        for (JsonDocumentation doc : dpa.getDocumentations()) {
            if (doc.getUuid() != null) {
                new DocumentationDAL().updateDocument(doc);
            } else {
                doc.setUuid(UUID.randomUUID().toString());
                new DocumentationDAL().saveDocument(doc);
            }
        }


        dpa.setPurposes(new DataSharingAgreementLogic().setUuidsAndSavePurpose(dpa.getPurposes()));
        dpa.setBenefits(new DataSharingAgreementLogic().setUuidsAndSavePurpose(dpa.getBenefits()));

        new MasterMappingDAL().saveDataProcessingAgreementMappings(dpa);

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

    private Response getSingleDPA(String uuid) throws Exception {
        DataProcessingAgreementEntity dpaEntity = new DataProcessingAgreementDAL().getDPA(uuid);

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

    public Response getLinkedDataFlows(String dpaUuid) throws Exception {

        List<String> dataFlowUuids = new SecurityMasterMappingDAL().getChildMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.DATAFLOW.getMapType());
        List<DataFlowEntity> ret = new ArrayList<>();

        if (!dataFlowUuids.isEmpty())
            ret = new DataFlowDAL().getDataFlowsFromList(dataFlowUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedCohorts(String dpaUuid) throws Exception {
        List<String> cohorts = new SecurityMasterMappingDAL().getChildMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.COHORT.getMapType());

        List<CohortEntity> ret = new ArrayList<>();

        if (!cohorts.isEmpty())
            ret = new SecurityCohortDAL().getCohortsFromList(cohorts);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedDataSets(String dpaUuid) throws Exception {
        List<String> datasets = new SecurityMasterMappingDAL().getChildMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.DATASET.getMapType());

        List<DatasetEntity> ret = new ArrayList<>();

        if (!datasets.isEmpty())
            ret = new SecurityDatasetDAL().getDataSetsFromList(datasets);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedRegions(String dsaUuid) throws Exception {

        List<String> regionUuids = new SecurityMasterMappingDAL().getParentMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.REGION.getMapType());

        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = new RegionDAL().getRegionsFromList(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getPublishers(String dsaUuid) throws Exception {

        List<String> publisherUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());

        List<OrganisationEntity> ret = new ArrayList<>();

        if (!publisherUuids.isEmpty())
            ret = OrganisationCache.getOrganisationDetails(publisherUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getPurposes(String dsaUuid) throws Exception {
        List<String> purposeUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PURPOSE.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (!purposeUuids.isEmpty())
            ret = new PurposeDAL().getPurposesFromList(purposeUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getBenefits(String dsaUuid) throws Exception {

        List<String> benefitUuids = new SecurityMasterMappingDAL().getChildMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.BENEFIT.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (!benefitUuids.isEmpty())
            ret = new PurposeDAL().getPurposesFromList(benefitUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response checkOrganisationIsPartOfDPA(String odsCode, boolean countOnly) throws Exception {

        List<DataProcessingAgreementEntity> matchingDpa = new DataProcessingAgreementDAL().getDataProcessingAgreementsForOrganisation(odsCode);

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
    }

    public Response checkOrganisationAndSystemIsPartOfDPA(String odsCode, String systemName) throws Exception {

        List<DataProcessingAgreementEntity> matchingDpa = new DataProcessingAgreementDAL().getDataProcessingAgreementsForOrganisationAndSystemType(odsCode, systemName);

        return Response
                .ok()
                .entity(matchingDpa)
                .build();
    }
}
