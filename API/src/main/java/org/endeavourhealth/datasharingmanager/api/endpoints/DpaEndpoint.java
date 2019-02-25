package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonDPA;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonDocumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/dpa")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to the data processing agreements")
public final class DpaEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DpaEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return either all data processing agreements if no parameter is provided or search for " +
            "data processing agreements using a UUID or a search term. Search matches on name or description of data processing agreement. " +
            "Returns a JSON representation of the matching set of Data processing agreement")
    public Response getDPA(@Context SecurityContext sc,
                        @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                        @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "DPA Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getDPA - list");
            return getDPAList();
        } else if (uuid != null){
            LOG.trace("getDPA - single - " + uuid);
            return getSingleDPA(uuid);
        } else {
            LOG.trace("Search DPA - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.Post")
    @Path("/")
    @ApiOperation(value = "Save a new data processing agreement or update an existing one.  Accepts a JSON representation " +
            "of a data processing agreement.")
    @RequiresAdmin
    public Response postDPA(@Context SecurityContext sc,
                         @ApiParam(value = "Json representation of data processing agreement to save or update") JsonDPA dpa) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "DPA",
                "DPA", dpa);

        if (dpa.getUuid() != null) {
            MasterMappingEntity.deleteAllMappings(dpa.getUuid());
            DataProcessingAgreementEntity.updateDPA(dpa);
        } else {
            dpa.setUuid(UUID.randomUUID().toString());
            DataProcessingAgreementEntity.saveDPA(dpa);
        }

        for (JsonDocumentation doc : dpa.getDocumentations()) {
            if (doc.getUuid() != null) {
                DocumentationEntity.updateDocument(doc);
            } else {
                doc.setUuid(UUID.randomUUID().toString());
                DocumentationEntity.saveDocument(doc);
            }
        }


        dpa.setPurposes(DsaEndpoint.setUuidsAndSavePurpose(dpa.getPurposes()));
        dpa.setBenefits(DsaEndpoint.setUuidsAndSavePurpose(dpa.getBenefits()));

        MasterMappingEntity.saveDataProcessingAgreementMappings(dpa);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(dpa.getUuid())
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete a data processing agreement based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteDPA(@Context SecurityContext sc,
                           @ApiParam(value = "UUID of the data processing agreement to be deleted") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "DPA",
                "DPA Id", uuid);

        DataProcessingAgreementEntity.deleteDPA(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.GetDataFlows")
    @Path("/dataflows")
    @ApiOperation(value = "Returns a list of Json representations of data flow agreements that are linked " +
            "to the data processing agreeement.  Accepts a UUID of a data processing agreement.")
    public Response getLinkedDataFlowsForDPA(@Context SecurityContext sc,
                                       @ApiParam(value = "UUID of data processing agreement") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "dataflows(s)",
                "DPA Id", uuid);

        return getLinkedDataFlows(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.GetCohorts")
    @Path("/cohorts")
    @ApiOperation(value = "Returns a list of Json representations of cohorts that are linked " +
            "to the data processing agreeement.  Accepts a UUID of a data processing agreement.")
    public Response getLinkedCohortsForDPA(@Context SecurityContext sc,
                                     @ApiParam(value = "UUID of data processing agreement") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "cohorts(s)",
                "DPA Id", uuid);

        return getLinkedCohorts(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.GetDataSets")
    @Path("/datasets")
    @ApiOperation(value = "Returns a list of Json representations of data sets that are linked " +
            "to the data processing agreeement.  Accepts a UUID of a data processing agreement.")
    public Response getLinkedDataSetsForDPA(@Context SecurityContext sc,
                                      @ApiParam(value = "UUID of data processing agreement") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Data Sets(s)",
                "DPA Id", uuid);

        return getLinkedDataSets(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.GetPublishers")
    @Path("/publishers")
    @ApiOperation(value = "Returns a list of Json representations of publishers that are linked " +
            "to the data processing agreement.  Accepts a UUID of a data processing agreement.")
    public Response getPublishersForDSA(@Context SecurityContext sc,
                                        @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "publisher(s)",
                "DSA Id", uuid);

        return getPublishers(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.GetRegions")
    @Path("/regions")
    @ApiOperation(value = "Returns a list of Json representations of regions that are linked " +
            "to the data processing agreement.  Accepts a UUID of a data processing agreement.")
    public Response getLinkedRegionsForDPA(@Context SecurityContext sc,
                                           @ApiParam(value = "UUID of DPA") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "dataflow(s)",
                "DSA Id", uuid);

        return getLinkedRegions(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.GetPurposes")
    @Path("/purposes")
    @ApiOperation(value = "Returns a list of Json representations of purposes that are linked " +
            "to the data processing agreement.  Accepts a UUID of a data processing agreement.")
    public Response getPurposesForDPA(@Context SecurityContext sc,
                                      @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "purpose(s)",
                "DSA Id", uuid);

        return getPurposes(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.getBenefits")
    @Path("/benefits")
    @ApiOperation(value = "Returns a list of Json representations of benefits that are linked " +
            "to the data processing agreement.  Accepts a UUID of a data processing agreement.")
    public Response getBenefitsForDPA(@Context SecurityContext sc,
                                      @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "benefits(s)",
                "DSA Id", uuid);

        return getBenefits(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.checkOrganisation")
    @Path("/checkOrganisation")
    @ApiOperation(value = "Checks whether an organisation is part of a data processing agreement. " +
            "Returns a list of data processing agreements")
    public Response checkOrganisation(@Context SecurityContext sc,
                                      @ApiParam(value = "ODS Code of organisation") @QueryParam("odsCode") String odsCode
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "check Organisation(s)",
                "ODS Code", odsCode);

        return checkOrganisationIsPartOfDPA(odsCode, false);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.checkOrganisationWithCount")
    @Path("/checkOrganisationWithCount")
    @ApiOperation(value = "Checks whether an organisation is part of a data processing agreement. " +
            "Returns a list of data processing agreements")
    public Response checkOrganisationWithCount(@Context SecurityContext sc,
                                      @ApiParam(value = "ODS Code of organisation") @QueryParam("odsCode") String odsCode
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "check Organisation(s)",
                "ODS Code", odsCode);

        return checkOrganisationIsPartOfDPA(odsCode, true);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.checkOrganisationAndSystem")
    @Path("/checkOrganisationAndSystem")
    @ApiOperation(value = "Checks whether an organisation and system is part of a data processing agreement. " +
            "Returns a list of data processing agreements")
    public Response checkOrganisationAndSystem(@Context SecurityContext sc,
                                               @ApiParam(value = "ODS Code of organisation") @QueryParam("odsCode") String odsCode,
                                               @ApiParam(value = "System Name") @QueryParam("systemName") String systemName
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "check Organisation(s)",
                "ODS Code", odsCode);

        return checkOrganisationAndSystemIsPartOfDPA(odsCode, systemName);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.getMarkersOfSubscribersOfDPA")
    @Path("/subscriberMarkers")
    @ApiOperation(value = "Returns a list of Json representations of addresses that are linked " +
            "to the subscribers in the corresponding DPA.  Accepts a UUID of an DPA.")
    public Response getMarkersOfSubscribersOfDPA(@Context SecurityContext sc,
                                                      @ApiParam(value = "UUID of DPA") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Marker(s)",
                "Region Id", uuid);

        return AddressEntity.getOrganisationMarkers(uuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.SUBSCRIBER.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.getMarkersOfPublishersOfDPA")
    @Path("/publisherMarkers")
    @ApiOperation(value = "Returns a list of Json representations of addresses that are linked " +
            "to the Publishers in the corresponding DPA.  Accepts a UUID of an DPA.")
    public Response getMarkersOfPublishersOfDPA(@Context SecurityContext sc,
                                                 @ApiParam(value = "UUID of DPA") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Marker(s)",
                "Region Id", uuid);

        return AddressEntity.getOrganisationMarkers(uuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());
    }

    private Response getDPAList() throws Exception {

        List<DataProcessingAgreementEntity> dpas = DataProcessingAgreementEntity.getAllDPAs();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    private Response getSingleDPA(String uuid) throws Exception {
        DataProcessingAgreementEntity dpaEntity = DataProcessingAgreementEntity.getDPA(uuid);

        return Response
                .ok()
                .entity(dpaEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataProcessingAgreementEntity> dpas = DataProcessingAgreementEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    private Response getLinkedDataFlows(String dpaUuid) throws Exception {

        List<String> dataFlowUuids = MasterMappingEntity.getChildMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.DATAFLOW.getMapType());
        List<DataFlowEntity> ret = new ArrayList<>();

        if (!dataFlowUuids.isEmpty())
            ret = DataFlowEntity.getDataFlowsFromList(dataFlowUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedCohorts(String dpaUuid) throws Exception {
        List<String> cohorts = MasterMappingEntity.getChildMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.COHORT.getMapType());

        List<CohortEntity> ret = new ArrayList<>();

        if (!cohorts.isEmpty())
            ret = CohortEntity.getCohortsFromList(cohorts);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedDataSets(String dpaUuid) throws Exception {
        List<String> datasets = MasterMappingEntity.getChildMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.DATASET.getMapType());

        List<DatasetEntity> ret = new ArrayList<>();

        if (!datasets.isEmpty())
            ret = DatasetEntity.getDataSetsFromList(datasets);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedRegions(String dsaUuid) throws Exception {

        List<String> regionUuids = MasterMappingEntity.getParentMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.REGION.getMapType());

        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = RegionEntity.getRegionsFromList(regionUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getPublishers(String dsaUuid) throws Exception {

        List<String> publisherUuids = MasterMappingEntity.getChildMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());

        List<OrganisationEntity> ret = new ArrayList<>();

        if (!publisherUuids.isEmpty())
            ret = OrganisationEntity.getOrganisationsFromList(publisherUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getPurposes(String dsaUuid) throws Exception {
        List<String> purposeUuids = MasterMappingEntity.getChildMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PURPOSE.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (!purposeUuids.isEmpty())
            ret = PurposeEntity.getPurposesFromList(purposeUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getBenefits(String dsaUuid) throws Exception {

        List<String> benefitUuids = MasterMappingEntity.getChildMappings(dsaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.BENEFIT.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (!benefitUuids.isEmpty())
            ret = PurposeEntity.getPurposesFromList(benefitUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response checkOrganisationIsPartOfDPA(String odsCode, boolean countOnly) throws Exception {

        List<DataProcessingAgreementEntity> matchingDpa = DataProcessingAgreementEntity.getDataProcessingAgreementsForOrganisation(odsCode);

        if (countOnly) {
            return Response
                    .ok()
                    .entity(matchingDpa.size())
                    .build();
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(matchingDpa)
                .build();
    }

    private Response checkOrganisationAndSystemIsPartOfDPA(String odsCode, String systemName) throws Exception {

        List<DataProcessingAgreementEntity> matchingDpa = DataProcessingAgreementEntity.getDataProcessingAgreementsForOrganisationAndSystemType(odsCode, systemName);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(matchingDpa)
                .build();
    }

}
