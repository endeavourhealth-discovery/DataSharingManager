package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDSA;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonRegion;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.DAL.AddressDAL;
import org.endeavourhealth.datasharingmanager.api.DAL.RegionDAL;
import org.endeavourhealth.datasharingmanager.api.Logic.DataSharingAgreementLogic;
import org.endeavourhealth.datasharingmanager.api.Logic.RegionLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/region")
@Api(description = "API endpoint related to the regions")
public final class RegionEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(RegionEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return either all regions if no parameter is provided or search for " +
            "regions using a UUID or a search term. Search matches on name or description of region. " +
            "Returns a JSON representation of the matching set of regions")
    public Response getRegion(@Context SecurityContext sc,
                        @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                        @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData,
                        @ApiParam(value = "Optional user Id to restrict based on region") @QueryParam("userId") String userId
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Organisation(s)",
                "Organisation Id", uuid,
                "SearchData", searchData);

        clearLogbackMarkers();
        return new RegionLogic().getRegion(uuid, searchData, userId);

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.Post")
    @Path("/")@ApiOperation(value = "Save a new region or update an existing one.  Accepts a JSON representation " +
            "of a region.")
    @RequiresAdmin
    public Response postRegion(@Context SecurityContext sc,
                               @HeaderParam("userProjectId") String userProjectId,
                               @ApiParam(value = "Json representation of region to save or update") JsonRegion region
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Region",
                "Region", region);

        clearLogbackMarkers();
        return new RegionLogic().postRegion(region, userProjectId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.Post")
    @Path("/updateMappings")
    @ApiOperation(value = "Updates the mappings.  Accepts a JSON representation of a region.")
    @RequiresAdmin
    public Response updateMappings(@Context SecurityContext sc,
                                   @HeaderParam("userProjectId") String userProjectId,
                                   @ApiParam(value = "Json representation of dsa to update") JsonRegion region
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Region",
                "Region", region);

        new RegionLogic().updateMappings(region, userProjectId);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(region.getUuid())
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete a region based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteRegion(@Context SecurityContext sc,
                                 @HeaderParam("userProjectId") String userProjectId,
                                 @ApiParam(value = "UUID of the regions to be deleted") @QueryParam("uuids") List<String> uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Region",
                "Region Id", uuids);

        for (String uuid : uuids) {
            new RegionDAL().deleteRegion(uuid, userProjectId);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.GetOrganisations")
    @Path("/organisations")
    @ApiOperation(value = "Returns a list of Json representations of organisations that are linked " +
            "to the region.  Accepts a UUID of a region.")
    public Response getOrganisationsForRegion(@Context SecurityContext sc,
                        @ApiParam(value = "UUID of the region") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Organisation(s)",
                "Region Id", uuid);

        return new RegionLogic().getRegionOrganisations(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.GetParentRegions")
    @Path("/parentRegions")
    @ApiOperation(value = "Returns a list of Json representations of parent regions that are linked " +
            "to the region.  Accepts a UUID of a region.")
    public Response getParentRegionsForRegion(@Context SecurityContext sc,
                                     @ApiParam(value = "UUID of the region") @QueryParam("uuid") String uuid,
                                              @ApiParam(value = "Optional user Id to restrict based on region") @QueryParam("userId") String userId
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Parent Region(s)",
                "Region Id", uuid);

        return new RegionLogic().getParentRegions(uuid, userId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.GetChildRegions")
    @Path("/childRegions")
    @ApiOperation(value = "Returns a list of Json representations of child regions that are linked " +
            "to the region.  Accepts a UUID of a region.")
    public Response getChildRegionsForRegion(@Context SecurityContext sc,
                                    @ApiParam(value = "UUID of the region") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Child Region(s)",
                "Region Id", uuid);

        return new RegionLogic().getChildRegions(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.GetSharingAgreements")
    @Path("/sharingAgreements")
    @ApiOperation(value = "Returns a list of Json representations of data sharing agreements that are linked " +
            "to the region.  Accepts a UUID of a region.")
    public Response getSharingAgreementsForRegion(@Context SecurityContext sc,
                                         @ApiParam(value = "UUID of the region") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Sharing Agreement(s)",
                "Region Id", uuid);

        return new RegionLogic().getSharingAgreements(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.GetSharingAgreements")
    @Path("/processingAgreements")
    @ApiOperation(value = "Returns a list of Json representations of data processing agreements that are linked " +
            "to the region.  Accepts a UUID of a region.")
    public Response getProcessingAgreementsForRegion(@Context SecurityContext sc,
                                                  @ApiParam(value = "UUID of the region") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Processing Agreement(s)",
                "Region Id", uuid);

        return new RegionLogic().getProcessingAgreements(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.getApiKey")
    @Path("/getApiKey")
    @ApiOperation(value = "Get the Google Maps API Key from the config database.")
    public Response getApiKey(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "get Api Key");

        JsonNode json = ConfigManager.getConfigurationAsJson("GoogleMapsAPI");

        return Response
                .ok()
                .entity(json)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RegionEndpoint.GetMarkers")
    @Path("/markers")
    @ApiOperation(value = "Returns a list of Json representations of addresses that are linked " +
            "to the organisations in the corresponding region.  Accepts a UUID of an organisation.")
    public Response getMarkersOfOrganisationsInRegion(@Context SecurityContext sc,
                                                      @ApiParam(value = "UUID of region") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Marker(s)",
                "Region Id", uuid);

        return new AddressDAL().getOrganisationMarkers(uuid, MapType.REGION.getMapType(), MapType.ORGANISATION.getMapType());
    }
}
