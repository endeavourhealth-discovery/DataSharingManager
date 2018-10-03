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
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonDSA;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonDocumentation;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonPurpose;
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

@Path("/dsa")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to the data sharing agreements")
public final class DsaEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DsaEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return either all data sharing agreements if no parameter is provided or search for " +
            "data sharing agreements using a UUID or a search term. Search matches on name or description of data sharing agreement. " +
            "Returns a JSON representation of the matching set of Data Flows")
    public Response getDSA(@Context SecurityContext sc,
                        @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                        @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DSA(s)",
                "DSA Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getDSA - list");
            return getDSAList();
        } else if (uuid != null){
            LOG.trace("getDSA - single - " + uuid);
            return getSingleDSA(uuid);
        } else {
            LOG.trace("Search DSA - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.Post")
    @Path("/")
    @ApiOperation(value = "Save a new data sharing agreement or update an existing one.  Accepts a JSON representation " +
            "of a data sharing agreement")
    @RequiresAdmin
    public Response postDSA(@Context SecurityContext sc,
                         @ApiParam(value = "Json representation of data sharing agreeement to save or update") JsonDSA dsa
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "DSA",
                "DSA", dsa);

        PurposeEntity.deleteAllPurposes(dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());

        if (dsa.getUuid() != null) {
            MasterMappingEntity.deleteAllMappings(dsa.getUuid());
            DataSharingAgreementEntity.updateDSA(dsa);
        } else {
            dsa.setUuid(UUID.randomUUID().toString());
            DataSharingAgreementEntity.saveDSA(dsa);
        }

        for (JsonDocumentation doc : dsa.getDocumentations()) {
            if (doc.getUuid() != null) {
                DocumentationEntity.updateDocument(doc);
            } else {
                doc.setUuid(UUID.randomUUID().toString());
                DocumentationEntity.saveDocument(doc);
            }
        }

        dsa.setPurposes(setUuidsAndSavePurpose(dsa.getPurposes()));
        dsa.setBenefits(setUuidsAndSavePurpose(dsa.getBenefits()));

        MasterMappingEntity.saveDataSharingAgreementMappings(dsa);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(dsa.getUuid())
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete a data sharing agreement based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteDSA(@Context SecurityContext sc,
                           @ApiParam(value = "UUID of the data sharing agreement to be deleted") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "DSA",
                "DSA Id", uuid);

        DataSharingAgreementEntity.deleteDSA(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.GetDataFlows")
    @Path("/dataflows")
    @ApiOperation(value = "Returns a list of Json representations of cohorts that are linked " +
            "to the data sharing agreement.  Accepts a UUID of a data sharing agreement.")
    public Response getLinkedCohortsForDSA(@Context SecurityContext sc,
                                     @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "dataflow(s)",
                "DSA Id", uuid);

        return getLinkedDataFlows(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.GetRegions")
    @Path("/regions")
    @ApiOperation(value = "Returns a list of Json representations of regions that are linked " +
            "to the data sharing agreement.  Accepts a UUID of a data sharing agreement.")
    public Response getLinkedRegionsForDSA(@Context SecurityContext sc,
                                     @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid
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
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.GetPublishers")
    @Path("/publishers")
    @ApiOperation(value = "Returns a list of Json representations of publishers that are linked " +
            "to the data sharing agreement.  Accepts a UUID of a data sharing agreement.")
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
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.GetSubscribers")
    @Path("/subscribers")
    @ApiOperation(value = "Returns a list of Json representations of subscribers that are linked " +
            "to the data sharing agreement.  Accepts a UUID of a data sharing agreement.")
    public Response getSubscribersForDSA(@Context SecurityContext sc,
                                   @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "subscriber(s)",
                "DSA Id", uuid);

        return getSubscribers(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.GetPurposes")
    @Path("/purposes")
    @ApiOperation(value = "Returns a list of Json representations of purposes that are linked " +
            "to the data sharing agreement.  Accepts a UUID of a data sharing agreement.")
    public Response getPurposesForDSA(@Context SecurityContext sc,
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
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.getBenefits")
    @Path("/benefits")
    @ApiOperation(value = "Returns a list of Json representations of benefits that are linked " +
            "to the data sharing agreement.  Accepts a UUID of a data sharing agreement.")
    public Response getBenefitsForDSA(@Context SecurityContext sc,
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
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.getMarkersOfSubscribersOfDPA")
    @Path("/subscriberMarkers")
    @ApiOperation(value = "Returns a list of Json representations of addresses that are linked " +
            "to the subscribers in the corresponding DSA.  Accepts a UUID of an DSA.")
    public Response getMarkersOfSubscribersOfDPA(@Context SecurityContext sc,
                                                 @ApiParam(value = "UUID of DSA") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Marker(s)",
                "DAS Id", uuid);

        return AddressEntity.getOrganisationMarkers(uuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.SUBSCRIBER.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.getMarkersOfPublishersOfDPA")
    @Path("/publisherMarkers")
    @ApiOperation(value = "Returns a list of Json representations of addresses that are linked " +
            "to the Publishers in the corresponding DSA.  Accepts a UUID of an DSA.")
    public Response getMarkersOfPublishersOfDPA(@Context SecurityContext sc,
                                                @ApiParam(value = "UUID of DSA") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Marker(s)",
                "DSA Id", uuid);

        return AddressEntity.getOrganisationMarkers(uuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DsaEndpoint.projects")
    @Path("/projects")
    @ApiOperation(value = "Returns a list of Json representations of projects that are linked " +
            "to the corresponding DSA.  Accepts a UUID of an DSA.")
    public Response projects(@Context SecurityContext sc,
                                                @ApiParam(value = "UUID of DSA") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Marker(s)",
                "DSA Id", uuid);

        return getProjects(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.checkOrganisationIsPartOfAgreement")
    @Path("/checkOrganisationIsPartOfAgreement")
    @ApiOperation(value = "Checks whether an organisation and system is part of a data processing agreement. " +
            "Returns a list of data processing agreements")
    public Response checkOrganisationIsPartOfAgreement(@Context SecurityContext sc,
                                               @ApiParam(value = "ODS Code of organisation") @QueryParam("odsCode") String odsCode
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "check Organisation(s)",
                "ODS Code", odsCode);

        return checkOrganisationIsPartOfDSA(odsCode);
    }

    private Response getDSAList() throws Exception {

        List<DataSharingAgreementEntity> dsas = DataSharingAgreementEntity.getAllDSAs();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dsas)
                .build();
    }

    private Response getSingleDSA(String uuid) throws Exception {
        DataSharingAgreementEntity dsaEntity = DataSharingAgreementEntity.getDSA(uuid);

        return Response
                .ok()
                .entity(dsaEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataSharingAgreementEntity> dsas = DataSharingAgreementEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dsas)
                .build();
    }

    private Response getLinkedDataFlows(String dsaUuid) throws Exception {

        List<String> dataFlowUuids = MasterMappingEntity.getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.DATAFLOW.getMapType());

        List<DataFlowEntity> ret = new ArrayList<>();

        if (dataFlowUuids.size() > 0)
            ret = DataFlowEntity.getDataFlowsFromList(dataFlowUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedRegions(String dsaUuid) throws Exception {

        List<String> regionUuids = MasterMappingEntity.getParentMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.REGION.getMapType());

        List<RegionEntity> ret = new ArrayList<>();

        if (regionUuids.size() > 0)
            ret = RegionEntity.getRegionsFromList(regionUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getPublishers(String dsaUuid) throws Exception {

        List<String> publisherUuids = MasterMappingEntity.getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());

        List<OrganisationEntity> ret = new ArrayList<>();

        if (publisherUuids.size() > 0)
            ret = OrganisationEntity.getOrganisationsFromList(publisherUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getSubscribers(String dsaUuid) throws Exception {

        List<String> subscriberUuids = MasterMappingEntity.getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.SUBSCRIBER.getMapType());

        List<OrganisationEntity> ret = new ArrayList<>();

        if (subscriberUuids.size() > 0)
            ret = OrganisationEntity.getOrganisationsFromList(subscriberUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getPurposes(String dsaUuid) throws Exception {
        List<String> purposeUuids = MasterMappingEntity.getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PURPOSE.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (purposeUuids.size() > 0)
            ret = PurposeEntity.getPurposesFromList(purposeUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getBenefits(String dsaUuid) throws Exception {

        List<String> benefitUuids = MasterMappingEntity.getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.BENEFIT.getMapType());

        List<PurposeEntity> ret = new ArrayList<>();

        if (benefitUuids.size() > 0)
            ret = PurposeEntity.getPurposesFromList(benefitUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getProjects(String dsaUuid) throws Exception {

        List<String> projectUuids = MasterMappingEntity.getChildMappings(dsaUuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PROJECT.getMapType());

        List<ProjectEntity> ret = new ArrayList<>();

        if (projectUuids.size() > 0)
            ret = ProjectEntity.getProjectsFromList(projectUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public static List<JsonPurpose> setUuidsAndSavePurpose(List<JsonPurpose> purposes) throws Exception {
        for (JsonPurpose purpose : purposes) {
            if (purpose.getUuid() == null) {
                purpose.setUuid(UUID.randomUUID().toString());
            }
            PurposeEntity.savePurpose(purpose);
        }

        return purposes;
    }

    private Response checkOrganisationIsPartOfDSA(String odsCode) throws Exception {

        List<String> matchingDpaEndpoints = DataSharingAgreementEntity.checkDataSharingAgreementsForOrganisation(odsCode);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(matchingDpaEndpoints)
                .build();
    }

}
