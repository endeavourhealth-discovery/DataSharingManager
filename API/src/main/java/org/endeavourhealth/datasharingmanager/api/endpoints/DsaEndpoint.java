package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDSA;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDocumentation;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.DAL.*;
import org.endeavourhealth.datasharingmanager.api.Logic.DataSharingAgreementLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

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

        clearLogbackMarkers();
        return new DataSharingAgreementLogic().getDSAs(uuid, searchData);

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

        clearLogbackMarkers();

        return new DataSharingAgreementLogic().postDSA(dsa);
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

        new DataSharingAgreementDAL().deleteDSA(uuid);

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
    public Response getLinkedDataflowsDSA(@Context SecurityContext sc,
                                     @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "dataflow(s)",
                "DSA Id", uuid);

        return new DataSharingAgreementLogic().getLinkedDataFlows(uuid);
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

        return new DataSharingAgreementLogic().getLinkedRegions(uuid);
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

        return new DataSharingAgreementLogic().getPublishers(uuid);
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

        return new DataSharingAgreementLogic().getSubscribers(uuid);
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

        return new DataSharingAgreementLogic().getPurposes(uuid);
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

        return new DataSharingAgreementLogic().getBenefits(uuid);
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

        return new AddressDAL().getOrganisationMarkers(uuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.SUBSCRIBER.getMapType());
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

        return new AddressDAL().getOrganisationMarkers(uuid, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());
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

        return new DataSharingAgreementLogic().getProjects(uuid);
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

        List<String> matchingDpaEndpoints = new DataSharingAgreementDAL().checkDataSharingAgreementsForOrganisation(odsCode);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(matchingDpaEndpoints)
                .build();
    }

}
