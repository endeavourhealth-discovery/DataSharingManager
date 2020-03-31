package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDPA;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDocumentation;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.DAL.AddressDAL;
import org.endeavourhealth.datasharingmanager.api.DAL.DataProcessingAgreementDAL;
import org.endeavourhealth.datasharingmanager.api.Logic.DataProcessingAgreementLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Path("/dpa")
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
                           @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData,
                           @ApiParam(value = "Optional user Id to restrict based on region") @QueryParam("userId") String userId,
                           @ApiParam(value = "Optional from region indicator") @QueryParam("fromRegion") String fromRegion
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "DPA Id", uuid,
                "SearchData", searchData);

        clearLogbackMarkers();
        if ("true".equals(fromRegion)) {
            return new DataProcessingAgreementLogic().getRegionlessDPAList();
        }

        return new DataProcessingAgreementLogic().getDPA(uuid, searchData, userId);
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
                            @HeaderParam("userProjectId") String userProjectId,
                            @ApiParam(value = "Json representation of data processing agreement to save or update") JsonDPA dpa) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "DPA",
                "DPA", dpa);

        clearLogbackMarkers();
        if (dpa.getPublishers() != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    new AddressDAL().getGeoLocationsForOrganisations(new ArrayList<>(dpa.getPublishers().keySet()));
                } catch (Exception e) {
                    // ignore error;
                }
            });
        }
        return new DataProcessingAgreementLogic().postDPA(dpa, userProjectId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.Post")
    @Path("/updateMappings")
    @ApiOperation(value = "Updates the mappings.  Accepts a JSON representation of a DPA.")
    @RequiresAdmin
    public Response updateMappings(@Context SecurityContext sc,
                                   @HeaderParam("userProjectId") String userProjectId,
                                   @ApiParam(value = "Json representation of dpa to update") JsonDPA dpa
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "DPA",
                "DPA", dpa);

        new DataProcessingAgreementLogic().updateMappings(dpa, userProjectId);

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
                              @HeaderParam("userProjectId") String userProjectId,
                              @ApiParam(value = "UUID of the data processing agreements to be deleted") @QueryParam("uuids") List<String> uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "DPA",
                "DPA Id", uuids);

        for (String uuid : uuids) {
            new DataProcessingAgreementDAL().deleteDPA(uuid, userProjectId);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
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

        return new DataProcessingAgreementLogic().getLinkedCohorts(uuid);
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

        return new DataProcessingAgreementLogic().getLinkedDataSets(uuid);
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

        return new DataProcessingAgreementLogic().getPublishers(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.GetRegions")
    @Path("/regions")
    @ApiOperation(value = "Returns a list of Json representations of regions that are linked " +
            "to the data processing agreement.  Accepts a UUID of a data processing agreement.")
    public Response getLinkedRegionsForDPA(@Context SecurityContext sc,
                                           @ApiParam(value = "UUID of DPA") @QueryParam("uuid") String uuid,
                                           @ApiParam(value = "Optional user Id to restrict based on region") @QueryParam("userId") String userId
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "dataflow(s)",
                "DSA Id", uuid);

        return new DataProcessingAgreementLogic().getLinkedRegions(uuid, userId);
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

        return new DataProcessingAgreementLogic().getPurposes(uuid);
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

        return new DataProcessingAgreementLogic().getBenefits(uuid);
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

        return new DataProcessingAgreementLogic().checkOrganisationIsPartOfDPA(odsCode, false);
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

        return new DataProcessingAgreementLogic().checkOrganisationIsPartOfDPA(odsCode, true);
    }

    /*@GET
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

        return new DataProcessingAgreementLogic().checkOrganisationAndSystemIsPartOfDPA(odsCode, systemName);
    }*/

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

        return new AddressDAL().getOrganisationMarkers(uuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.SUBSCRIBER.getMapType());
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

        return new AddressDAL().getOrganisationMarkers(uuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DpaEndpoint.addDocument")
    @Path("/addDocument")
    @ApiOperation(value = "Post the uploaded document")
    public Response getDescription(@Context SecurityContext sc,
                                   @HeaderParam("userProjectId") String userProjectId,
                                   @ApiParam(value = "uuid") @QueryParam("uuid") String uuid,
                                   @ApiParam(value = "Document object") JsonDocumentation document) throws Exception {

        super.setLogbackMarkers(sc);
        return new DataProcessingAgreementLogic().addDocument(uuid, document, userProjectId);
    }
}
