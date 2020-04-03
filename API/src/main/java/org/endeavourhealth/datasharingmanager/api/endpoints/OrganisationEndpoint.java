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
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonFileUpload;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonOrganisation;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.OrganisationEntity;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.DAL.OrganisationDAL;
import org.endeavourhealth.datasharingmanager.api.Logic.OrganisationLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/organisation")
@Api(description = "API endpoint related to the organisations and services.  " +
        "Services are just organisations with a flag indicating they are a service.")
public final class OrganisationEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return either all organisations if no parameter is provided or search for " +
            "organisations using a UUID or a search term. Search matches on name or ODS code of organisations. " +
            "Returns a JSON representation of the matching set of organisations")
    public Response getOrganisation(@Context SecurityContext sc,
                        @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                        @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData,
                        @ApiParam(value = "Optional string of 'services' to indicate you want to search for services instead of organisations")
                            @QueryParam("searchType") String searchType,
                                    @ApiParam(value = "Organisation type (defaults to all if not provided)")@QueryParam("organisationType") byte organisationType,
                        @ApiParam(value = "Optional page number (defaults to 1 if not provided)") @QueryParam("pageNumber") Integer pageNumber,
                        @ApiParam(value = "Optional page size (defaults to 20 if not provided)")@QueryParam("pageSize") Integer pageSize,
                        @ApiParam(value = "Optional order column (defaults to name if not provided)")@QueryParam("orderColumn") String orderColumn,
                        @ApiParam(value = "Optional ordering direction (defaults to ascending if not provided)")@QueryParam("descending") boolean descending) throws Exception {

        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Organisation(s)",
                "Organisation Id", uuid,
                "SearchData", searchData);

        clearLogbackMarkers();

        return new OrganisationLogic().getOrganisation(uuid, searchData, searchType, organisationType, pageNumber, pageSize, orderColumn, descending, SecurityUtils.getCurrentUserId(sc));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.Post")
    @Path("/")
    @ApiOperation(value = "Save a new organisation or update an existing one.  Accepts a JSON representation " +
            "of a organisation.")
    @RequiresAdmin
    public Response postOrganisation(@Context SecurityContext sc,
                                     @HeaderParam("userProjectId") String userProjectId,
                                     @ApiParam(value = "Json representation of organisation to save or update") JsonOrganisation organisation
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", organisation);

        clearLogbackMarkers();
        return new OrganisationLogic().postOrganisation(organisation, userProjectId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.Post")
    @Path("/updateMappings")
    @ApiOperation(value = "Save a new organisation or update an existing one.  Accepts a JSON representation " +
            "of a organisation.")
    @RequiresAdmin
    public Response updateMappings(@Context SecurityContext sc,
                                     @HeaderParam("userProjectId") String userProjectId,
                                     @ApiParam(value = "Json representation of organisation to save or update") JsonOrganisation organisation
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", organisation);

        clearLogbackMarkers();
        return new OrganisationLogic().updateMappings(organisation, userProjectId);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete an organisation based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteOrganisation(@Context SecurityContext sc,
                                       @HeaderParam("userProjectId") String userProjectId,
                                       @ApiParam(value = "UUID of the organisations to be deleted") @QueryParam("uuids") List<String> uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Organisation",
                "Organisation Id", uuids);

        new OrganisationLogic().deleteOrganisation(uuids, userProjectId, SecurityUtils.getCurrentUserId(sc));

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.GetRegions")
    @Path("/regions")
    @ApiOperation(value = "Returns a list of Json representations of regions that are linked " +
            "to the organisation.  Accepts a UUID of an organisation.")
    public Response getRegionsForOrganisation(@Context SecurityContext sc,
                        @ApiParam(value = "UUID of organisation") @QueryParam("uuid") String uuid,
                        @ApiParam(value = "Optional user Id to restrict based on region") @QueryParam("userId") String userId
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Region(s)",
                "Organisation Id", uuid);

        return new OrganisationLogic().getRegionsForOrganisation(uuid, userId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.GetChildOrganisations")
    @Path("/childOrganisations")
    @ApiOperation(value = "Returns a list of Json representations of child organisations that are linked " +
            "to the organisation.  Accepts a UUID of an organisation.")
    public Response getChildOrganisationsForOrganisation(@Context SecurityContext sc,
                                          @ApiParam(value = "UUID of organisation") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Child Organisations(s)",
                "Organisation Id", uuid);


        return new OrganisationLogic().getChildOrganisations(uuid, MapType.ORGANISATION.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.GetServices")
    @Path("/services")
    @ApiOperation(value = "Returns a list of Json representations of services that are linked " +
            "to the organisation.  Accepts a UUID of an organisation.")
    public Response getServicesForOrganisation(@Context SecurityContext sc,
                                @ApiParam(value = "UUID of organisation") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "services(s)",
                "Organisation Id", uuid);

        return new OrganisationLogic().getChildOrganisations(uuid, MapType.SERVICE.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.GetParentOrganisations")
    @Path("/parentOrganisations")
    @ApiOperation(value = "Returns a list of Json representations of parent organisations that are linked " +
            "to the organisation.  Accepts a UUID of an organisation.")
    public Response getParentOrganisationsForOrganisation(@Context SecurityContext sc,
                                           @ApiParam(value = "UUID of organisation") @QueryParam("uuid") String uuid,
                                           @ApiParam(value = "Is the organisation a service?") @QueryParam("isService") String isService) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Parent Organisations(s)",
                "Organisation Id", uuid);

        Short orgType = MapType.ORGANISATION.getMapType();
        if (isService.equals("1"))
            orgType = MapType.SERVICE.getMapType();


        return new OrganisationLogic().getParentOrganisations(uuid, orgType);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.getDPAsOrganisationPublishing")
    @Path("/dpasPublishing")
    @ApiOperation(value = "Returns a list of Json representations of DPAs that " +
            "the organisation is publishing to.  Accepts a UUID of an organisation.")
    public Response getDPAsOrganisationPublishing(@Context SecurityContext sc,
                                              @ApiParam(value = "UUID of organisation") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "Organisation Id", uuid);

        return new OrganisationLogic().getDPAsOrganisationPublishingTo(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.getDPAsOrganisationPublishingFromList")
    @Path("/dpasPublishingFromList")
    @ApiOperation(value = "Returns a list of Json representations of DPAs that " +
            "the organisation is publishing to.  Accepts a UUID of an organisation.")
    public Response getDPAsOrganisationPublishingFromList(@Context SecurityContext sc,
                                                  @ApiParam(value = "UUID of organisation") @QueryParam("uuids") List<String > uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "Organisation Id", uuids);

        return new OrganisationLogic().getDPAsOrganisationPublishingToFromList(uuids);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.getProjectsOrganisationPublishingFromList")
    @Path("/projectsPublishingFromList")
    @ApiOperation(value = "Returns a list of Json representations of projects that " +
            "the organisation is publishing to.  Accepts a UUID of an organisation.")
    public Response getProjectsOrganisationPublishingFromList(@Context SecurityContext sc,
                                                          @ApiParam(value = "UUID of organisation") @QueryParam("uuids") List<String > uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "Organisation Id", uuids);

        return new OrganisationLogic().getProjectsOrganisationPublishingToFromList(uuids);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.getDSAsOrganisationPublishing")
    @Path("/dsasPublishing")
    @ApiOperation(value = "Returns a list of Json representations of DSAs that " +
            "the organisation is publishing to.  Accepts a UUID of an organisation.")
    public Response getDSAsOrganisationPublishing(@Context SecurityContext sc,
                                                  @ApiParam(value = "UUID of organisation") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DSA(s)",
                "Organisation Id", uuid);

        return new OrganisationLogic().getDSAsOrganisationPublishingTo(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.getDSAsOrganisationPublishingFromList")
    @Path("/dsasPublishingFromList")
    @ApiOperation(value = "Returns a list of Json representations of DSAs that " +
            "the organisation is publishing to.  Accepts a UUID of an organisation.")
    public Response getDSAsOrganisationPublishingFromList(@Context SecurityContext sc,
                                                  @ApiParam(value = "UUID of organisation") @QueryParam("uuids") List<String> uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DSA(s)",
                "Organisation Id", uuids);

        return new OrganisationLogic().getDSAsOrganisationPublishingToFromList(uuids);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.getDPAsOrganisationPublishing")
    @Path("/dsasSubscribing")
    @ApiOperation(value = "Returns a list of Json representations of DSAs that " +
            "the organisation is subscribing to.  Accepts a UUID of an organisation.")
    public Response getDSAsOrganisationSubscribing(@Context SecurityContext sc,
                                                  @ApiParam(value = "UUID of organisation") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "Organisation Id", uuid);

        return new OrganisationLogic().getDSAsOrganisationSubscribingTo(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.getDSAsOrganisationSubscribingFromList")
    @Path("/dsasSubscribingFromList")
    @ApiOperation(value = "Returns a list of Json representations of DSAs that " +
            "the organisation is subscribing to.  Accepts a UUID of an organisation.")
    public Response getDSAsOrganisationSubscribingFromList(@Context SecurityContext sc,
                                                   @ApiParam(value = "UUID of organisation") @QueryParam("uuids") List<String> uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "Organisation Id", uuids);

        return new OrganisationLogic().getDSAsOrganisationSubscribingToFromList(uuids);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="organisation.GetAddresses")
    @Path("/addresses")
    @ApiOperation(value = "Returns a list of Json representations of addresses that are linked " +
            "to the organisation.  Accepts a UUID of an organisation.")
    public Response getAddressesForOrganisation(@Context SecurityContext sc,
                                 @ApiParam(value = "UUID of organisation") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Address(es)",
                "Organisation Id", uuid);

        return new OrganisationLogic().getOrganisationAddressList(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.GetEditedBulks")
    @Path("/editedBulks")
    @ApiOperation(value = "Returns a list of Json representations of bulk added organisations that have been edited.")
    @RequiresAdmin
    public Response getUpdatedBulkOrganisations(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", null);

        List<OrganisationEntity> organisations = new OrganisationDAL().getUpdatedBulkOrganisations();

        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.GetConflicts")
    @Path("/conflicts")
    @ApiOperation(value = "Returns a list of Json representations of conflicted organisations after a bulk addition.")
    @RequiresAdmin
    public Response getConflictedOrganisations(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", null);

        List<OrganisationEntity> organisations = new OrganisationDAL().getConflictedOrganisations();

        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.getStatistics")
    @Path("/statistics")
    @RequiresAdmin
    @ApiOperation(value = "Get a Json representation of statistics relevant to the type of entity.")
    public Response getStatistics(@Context SecurityContext sc,
                                  @ApiParam(value = "Entity type") @QueryParam("type") String type
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation Statistics",
                "Organisation", null);

        LOG.trace("Statistics obtained");
        return new OrganisationLogic().generateStatistics(type);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.DeleteBulks")
    @Path("/deleteBulks")
    @ApiOperation(value = "Deletes all un-edited organisations that have been bulk imported.")
    @RequiresAdmin
    public Response deleteBulkOrganisations(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Organisation",
                "Organisation Id", null);

        new OrganisationDAL().deleteUneditedBulkOrganisations();

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.UploadBulkOrganisations")
    @Path("/upload")
    @ApiOperation(value = "Upload a CSV file from TRUD containing organisations to bulk import.")
    @RequiresAdmin
    public Response postOrganisationCSVFile(@Context SecurityContext sc,
                         @ApiParam(value = "Json representation of csv file to process") JsonFileUpload fileUpload
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", fileUpload.getName());

        return new OrganisationLogic().processCSVFile(fileUpload);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.startUpload")
    @Path("/startUpload")
    @ApiOperation(value = "Saves the organisations that have been processed from the CSV file. " +
            "Prevents other uploads from taking place simultaniously")
    @RequiresAdmin
    public Response startUpload(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Start Upload",
                "Organisation", null);

        return new OrganisationLogic().startUpload();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.saveMappings")
    @Path("/saveMappings")
    @ApiOperation(value = "Save Mappings between organisations to the database.  Part of the bulk upload process")
    public Response saveMappings(@Context SecurityContext sc,
                                      @ApiParam(value = "Number of mappings to save in this batch") @QueryParam("limit") Integer limit) throws Exception {

        return new OrganisationLogic().saveBulkMappings(limit);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.endUpload")
    @Path("/endUpload")
    @ApiOperation(value = "Ends the upload process allowing other uploads to start.")
    @RequiresAdmin
    public Response endUpload(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "End Upload",
                "Organisation", null);

        return new OrganisationLogic().endUpload();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.searchCount")
    @Path("/searchCount")
    @ApiOperation(value = "When using server side pagination, this returns the total count of the results of the query")
    public Response getOrganisationSearchCount(@Context SecurityContext sc,
                                               @ApiParam(value = "expression to filter organisations by") @QueryParam("expression") String expression,
                                               @ApiParam(value = "Searching for organisations or services") @QueryParam("searchType") String searchType
    ) throws Exception {

        boolean searchServices = false;
        if (searchType != null && searchType.equals("services"))
            searchServices = true;

        if (expression == null)
            expression = "";

        return new OrganisationLogic().getTotalNumberOfOrganisations(expression, searchServices);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.organisationTypes")
    @Path("/organisationTypes")
    @ApiOperation(value = "Get a list of organisation types")
    public Response getOrganisationTypes(@Context SecurityContext sc) throws Exception {

        return new OrganisationLogic().getOrganisationTypes();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.organisationByType")
    @Path("/organisationByType")
    @ApiOperation(value = "Get a list of organisation types")
    public Response getOrganisationsByType(@Context SecurityContext sc,
                                         @ApiParam(value = "Organisation Type to filter by") @QueryParam("type") byte type
    ) throws Exception {

        return new OrganisationLogic().getOrganisationsByType(type);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.getMultipleOrganisationsFromODSList")
    @Path("/getMultipleOrganisationsFromODSList")
    @ApiOperation(value = "Returns a list of Json representations of Organisations based on" +
            "a list of ODS codes.  Accepts a list of ODS codes.")
    public Response getMultipleOrganisationsFromODSList(@Context SecurityContext sc,
                                                           @ApiParam(value = "ODS Codes") @QueryParam("odsCodes") List<String> odsCodes
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "ODS Codes",
                "ODS Code", odsCodes);

        return new OrganisationLogic().getMultipleOrganisationsFromODSList(odsCodes);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.searchOrganisationsInParentRegion")
    @Path("/searchOrganisationsInParentRegion")
    @ApiOperation(value = "Returns a list of Json representations of Organisations based on" +
            "a search term. Only searches in organisations that are part of the parent region.")
    public Response searchOrganisationsInParentRegion(@Context SecurityContext sc,
                                                        @ApiParam(value = "region UUID") @QueryParam("regionUUID") String regionUUID,
                                                        @ApiParam(value = "search term") @QueryParam("searchTerm") String searchTerm
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "regionUUID",
                "regionUUID", regionUUID, "searchTerm", searchTerm);

        return new OrganisationLogic().searchOrganisationsInRegion(regionUUID, searchTerm, null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.searchPublishersFromDSA")
    @Path("/searchPublishersFromDSA")
    @ApiOperation(value = "Returns a list of Json representations of Organisations based on" +
            "a search term. Only searches in publishers that are part of the DSA.")
    public Response searchPublishersInDSA(@Context SecurityContext sc,
                                                        @ApiParam(value = "dsa UUID") @QueryParam("dsaUUID") String dsaUUID,
                                                        @ApiParam(value = "search term") @QueryParam("searchTerm") String searchTerm
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "regionUUID",
                "regionUUID", dsaUUID, "searchTerm", searchTerm);

        return new OrganisationLogic().searchPublishersInDSA(dsaUUID, searchTerm, null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.searchSubscribersFromDSA")
    @Path("/searchSubscribersFromDSA")
    @ApiOperation(value = "Returns a list of Json representations of Organisations based on" +
            "a search term. Only searches in subscribers that are part of the DSA.")
    public Response searchSubscribersInDSA(@Context SecurityContext sc,
                                            @ApiParam(value = "dsa UUID") @QueryParam("dsaUUID") String dsaUUID,
                                            @ApiParam(value = "search term") @QueryParam("searchTerm") String searchTerm
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "regionUUID",
                "regionUUID", dsaUUID, "searchTerm", searchTerm);

        return new OrganisationLogic().searchSubscribersInDSA(dsaUUID, searchTerm, null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.getAllOrganisationInAllChildRegions")
    @Path("/getAllOrganisationInAllChildRegions")
    @ApiOperation(value = "Returns a list of Json representations of Organisations based on" +
            "a search term. Only searches in subscribers that are part of the DSA.")
    public Response getAllOrganisationInAllChildRegions(@Context SecurityContext sc,
                                           @ApiParam(value = "region UUID") @QueryParam("regionUUID") String regionUUID
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "regionUUID",
                "regionUUID", regionUUID);

        return new OrganisationLogic().getAllOrganisationsInRegionAndChildRegions(regionUUID);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.searchOrganisationsInParentRegionWithOdsList")
    @Path("/searchOrganisationsInParentRegionWithOdsList")
    @ApiOperation(value = "Returns a list of Json representations of Organisations based on" +
            "a search term. Only searches in organisations that are part of the parent region.")
    public Response searchOrganisationsInParentRegionWithOdsList(@Context SecurityContext sc,
                                                      @ApiParam(value = "region UUID") @QueryParam("regionUUID") String regionUUID,
                                                      @ApiParam(value = "ODS Codes") @QueryParam("odsCodes") List<String> odsCodes
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "regionUUID",
                "regionUUID", regionUUID, "searchTerm", odsCodes);

        return new OrganisationLogic().searchOrganisationsInRegion(regionUUID, null, odsCodes);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.searchPublishersFromDSAWithOdsList")
    @Path("/searchPublishersFromDSAWithOdsList")
    @ApiOperation(value = "Returns a list of Json representations of Organisations based on" +
            "a search term. Only searches in publishers that are part of the DSA.")
    public Response searchPublishersFromDSAWithOdsList(@Context SecurityContext sc,
                                          @ApiParam(value = "dsa UUID") @QueryParam("dsaUUID") String dsaUUID,
                                          @ApiParam(value = "ODS Codes") @QueryParam("odsCodes") List<String> odsCodes
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "regionUUID",
                "regionUUID", dsaUUID, "searchTerm", odsCodes);

        return new OrganisationLogic().searchPublishersInDSA(dsaUUID, null, odsCodes);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.searchSubscribersFromDSAWithOdsList")
    @Path("/searchSubscribersFromDSAWithOdsList")
    @ApiOperation(value = "Returns a list of Json representations of Organisations based on" +
            "a search term. Only searches in subscribers that are part of the DSA.")
    public Response searchSubscribersFromDSAWithOdsList(@Context SecurityContext sc,
                                           @ApiParam(value = "dsa UUID") @QueryParam("dsaUUID") String dsaUUID,
                                           @ApiParam(value = "ODS Codes") @QueryParam("odsCodes") List<String> odsCodes
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "regionUUID",
                "regionUUID", dsaUUID, "searchTerm", odsCodes);

        return new OrganisationLogic().searchSubscribersInDSA(dsaUUID, null, odsCodes);
    }

}
