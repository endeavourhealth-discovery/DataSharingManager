package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
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
import org.endeavourhealth.datasharingmanager.api.metrics.DataSharingManagerMetricListener;
import org.endeavourhealth.datasharingmanager.api.utility.CsvHelper;
import org.endeavourhealth.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.datasharingmanagermodel.models.enums.OrganisationType;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonAddress;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonFileUpload;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonOrganisation;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Path("/organisation")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to the organisations and services.  " +
        "Services are just organisations with a flag indicating they are a service.")
public final class OrganisationEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static List<MasterMappingEntity> bulkUploadMappings = new ArrayList<>();
    private static HashMap<String, String> bulkOrgMap = new HashMap<>();
    private static HashMap<String, List<String>> childParentMap = new HashMap<>();
    private static boolean uploadInProgress = false;

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);
    private static final MetricRegistry metricRegistry = DataSharingManagerMetricListener.dataSharingManagerMetricRegistry;

    private Integer defaultPageNumber = 1;
    private Integer defaultPageSize = 20;
    private String defaultOrderColumn = "name";
    private String defaultSearchData = "";


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

        boolean searchServices = false;
        byte orgType = -1;
        if (searchType != null && searchType.equals("services"))
            searchServices = true;

        if (pageNumber == null)
            pageNumber = defaultPageNumber;
        if (pageSize == null)
            pageSize = defaultPageSize;
        if (orderColumn == null)
            orderColumn = defaultOrderColumn;
        if (searchData == null)
            searchData = defaultSearchData;

        if (uuid != null) {
            LOG.trace("getOrganisation - single - " + uuid);
            return getSingleOrganisation(uuid);
        } else if (!Strings.isNullOrEmpty(searchData)) {
            return searchOrganisations(searchData, searchServices, organisationType, pageNumber, pageSize, orderColumn, descending, SecurityUtils.getCurrentUserId(sc));
        } else {
            LOG.trace("get all Organisations - " + searchData + searchType);
            return getOrganisations(searchData, searchServices, organisationType, pageNumber, pageSize, orderColumn, descending);
        }
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
                         @ApiParam(value = "Json representation of organisation to save or update") JsonOrganisation organisation
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", organisation);

        if (organisation.getUuid() != null) {
            MasterMappingEntity.deleteAllMappings(organisation.getUuid());
            OrganisationEntity.updateOrganisation(organisation);
        } else {
            String hashString = organisation.getName() + organisation.getOdsCode();
            if (organisation.getIsService().equals("1")) {
                hashString += "service";
            }
            organisation.setUuid(UUID.nameUUIDFromBytes(hashString.getBytes()).toString());
            OrganisationEntity.saveOrganisation(organisation);
        }


        //Process Mappings
        MasterMappingEntity.saveOrganisationMappings(organisation);

        AddressEntity.deleteAddressForOrganisations(organisation.getUuid());
        List<JsonAddress> addresses = organisation.getAddresses();
        if (!addresses.isEmpty()) {
            for (JsonAddress address : addresses) {
                if (address.getOrganisationUuid() == null)
                    address.setOrganisationUuid(organisation.getUuid());

                if (address.getUuid() == null) {
                    address.setUuid(UUID.randomUUID().toString());
                }
                AddressEntity.saveAddress(address);
                /*else
                    AddressEntity.updateAddress(address);*/

                AddressEntity.getGeolocation(address);
            }

        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(organisation.getUuid())
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.OrganisationEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete an organisation based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteOrganisation(@Context SecurityContext sc,
                                       @ApiParam(value = "UUID of the organisation to be deleted") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Organisation",
                "Organisation Id", uuid);

        OrganisationEntity.deleteOrganisation(uuid);

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
                        @ApiParam(value = "UUID of organisation") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Region(s)",
                "Organisation Id", uuid);

        return getRegionsForOrganisation(uuid);
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


        return getChildOrganisations(uuid, MapType.ORGANISATION.getMapType());
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

        return getChildOrganisations(uuid, MapType.SERVICE.getMapType());
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


        return getParentOrganisations(uuid, orgType);
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

        return getDPAsOrganisationPublishingTo(uuid);
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

        return getDPAsOrganisationPublishingToFromList(uuids);
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

        return getDSAsOrganisationPublishingTo(uuid);
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

        return getDSAsOrganisationPublishingToFromList(uuids);
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

        return getDSAsOrganisationSubscribingTo(uuid);
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

        return getDSAsOrganisationSubscribingToFromList(uuids);
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

        return getOrganisationAddressList(uuid);
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

        List<OrganisationEntity> organisations = OrganisationEntity.getUpdatedBulkOrganisations();

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

        List<OrganisationEntity> organisations = OrganisationEntity.getConflictedOrganisations();

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
        return generateStatistics(type);
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

        OrganisationEntity.deleteUneditedBulkOrganisations();

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

        return processCSVFile(fileUpload);
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

        return startUpload();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.saveMappings")
    @Path("/saveMappings")
    @ApiOperation(value = "Save Mappings between organisations to the database.  Part of the bulk upload process")
    public Response saveMappings(@Context SecurityContext sc,
                                      @ApiParam(value = "Number of mappings to save in this batch") @QueryParam("limit") Integer limit) throws Exception {

        return saveBulkMappings(limit);
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

        return endUpload();
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

        return getTotalNumberOfOrganisations(expression, searchServices);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.organisationTypes")
    @Path("/organisationTypes")
    @ApiOperation(value = "Get a list of organisation types")
    public Response getOrganisationTypes(@Context SecurityContext sc) throws Exception {

        return getOrganisationTypes();
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

        return getOrganisationsByType(type);
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

        return getMultipleOrganisationsFromODSList(odsCodes);
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

        return searchOrganisationsInRegion(regionUUID, searchTerm, null);
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

        return searchPublishersInDSA(dsaUUID, searchTerm, null);
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

        return searchSubscribersInDSA(dsaUUID, searchTerm, null);
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

        return searchOrganisationsInRegion(regionUUID, null, odsCodes);
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

        return searchPublishersInDSA(dsaUUID, null, odsCodes);
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

        return searchSubscribersInDSA(dsaUUID, null, odsCodes);
    }

    private Response getOrganisationsByType(byte type) throws Exception {
        List<OrganisationEntity> organisations = OrganisationEntity.getOrganisationByType(type);

        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    private Response getOrganisationTypes() throws Exception {
        List<OrganisationTypeEntity> organisationTypes = OrganisationTypeEntity.getAllOrganisationTypes();

        return Response
                .ok()
                .entity(organisationTypes)
                .build();
    }

    private Response getTotalNumberOfOrganisations(String expression, boolean searchServices) throws Exception {
        Long count = OrganisationEntity.getTotalNumberOfOrganisations(expression, searchServices);

        return Response
                .ok()
                .entity(count)
                .build();
    }

    private Response getSingleOrganisation(String uuid) throws Exception {
        OrganisationEntity organisationEntity = OrganisationEntity.getOrganisation(uuid);

        return Response
                .ok()
                .entity(organisationEntity)
                .build();

    }

    private Response getOrganisations(String searchData, boolean searchServices, byte organisationType,
                            Integer pageNumber, Integer pageSize,
                            String orderColumn, boolean descending) throws Exception {
        List<OrganisationEntity> organisations = OrganisationEntity.getOrganisations(searchData, searchServices,
                organisationType, pageNumber, pageSize, orderColumn, descending);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    private Response searchOrganisations(String searchData, boolean searchServices, byte organisationType,
                                      Integer pageNumber, Integer pageSize,
                                      String orderColumn, boolean descending, UUID userId) throws Exception {
        List<OrganisationEntity> organisations = OrganisationEntity.searchOrganisations(searchData, searchServices,
                organisationType, pageNumber, pageSize, orderColumn, descending, userId);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    private Response getRegionsForOrganisation(String organisationUuid) throws Exception {

        List<String> regionUuids = MasterMappingEntity.getParentMappings(organisationUuid, MapType.ORGANISATION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = RegionEntity.getRegionsFromList(regionUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getDPAsOrganisationPublishingTo(String organisationUuid) throws Exception {

        List<String> dpaUUIDs = MasterMappingEntity.getParentMappings(organisationUuid, MapType.PUBLISHER.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (!dpaUUIDs.isEmpty())
            ret = DataProcessingAgreementEntity.getDPAsFromList(dpaUUIDs);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getDPAsOrganisationPublishingToFromList(List<String> organisationUuids) throws Exception {

        List<String> dpaUUIDs = MasterMappingEntity.getParentMappingsFromList(organisationUuids, MapType.PUBLISHER.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (!dpaUUIDs.isEmpty())
            ret = DataProcessingAgreementEntity.getDPAsFromList(dpaUUIDs);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getDSAsOrganisationSubscribingTo(String organisationUuid) throws Exception {

        List<String> dsaUuids = MasterMappingEntity.getParentMappings(organisationUuid, MapType.SUBSCRIBER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUuids.isEmpty())
            ret = DataSharingAgreementEntity.getDSAsFromList(dsaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getDSAsOrganisationSubscribingToFromList(List<String> organisationUuids) throws Exception {

        List<String> dsaUuids = MasterMappingEntity.getParentMappingsFromList(organisationUuids, MapType.SUBSCRIBER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUuids.isEmpty())
            ret = DataSharingAgreementEntity.getDSAsFromList(dsaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getDSAsOrganisationPublishingTo(String organisationUuid) throws Exception {

        List<String> dsaUUIds = MasterMappingEntity.getParentMappings(organisationUuid, MapType.PUBLISHER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUUIds.isEmpty())
            ret = DataSharingAgreementEntity.getDSAsFromList(dsaUUIds);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getDSAsOrganisationPublishingToFromList(List<String> organisationUuids) throws Exception {

        List<String> dsaUUIds = MasterMappingEntity.getParentMappingsFromList(organisationUuids, MapType.PUBLISHER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUUIds.isEmpty())
            ret = DataSharingAgreementEntity.getDSAsFromList(dsaUUIds);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response generateStatistics(String type) throws Exception {
        List<JsonStatistics> stats = OrganisationEntity.getStatisticsForType(type);

        return Response
                .ok()
                .entity(stats)
                .build();
    }

    private Response getOrganisationAddressList(String uuid) throws Exception {

        List<AddressEntity> addresses = AddressEntity.getAddressesForOrganisation(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(addresses)
                .build();
    }

    private Response getChildOrganisations(String organisationUuid, Short organisationType) throws Exception {

        List<String> organisationUuids = MasterMappingEntity.getChildMappings(organisationUuid, MapType.ORGANISATION.getMapType(), organisationType);
        List<OrganisationEntity> ret = new ArrayList<>();

        if (!organisationUuids.isEmpty())
            ret = OrganisationEntity.getOrganisationsFromList(organisationUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getParentOrganisations(String organisationUuid, Short orgType) throws Exception {

        List<String> organisationUuids = MasterMappingEntity.getParentMappings(organisationUuid, orgType, MapType.ORGANISATION.getMapType());
        List<OrganisationEntity> ret = new ArrayList<>();

        if (!organisationUuids.isEmpty())
            ret = OrganisationEntity.getOrganisationsFromList(organisationUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getMultipleOrganisationsFromODSList(List<String> odsCodes) throws Exception {

        List<OrganisationEntity> matchingOrganisations = OrganisationEntity.searchOrganisationsFromOdsList(odsCodes);

        return Response
                .ok()
                .entity(matchingOrganisations)
                .build();
    }

    private Response searchOrganisations(List<String> organisationUuids, String searchTerm) throws Exception {
        List<OrganisationEntity> baseOrganisationList = new ArrayList<>();
        List<OrganisationEntity> matchingOrgs = new ArrayList<>();

        if (!organisationUuids.isEmpty()) {
            baseOrganisationList = OrganisationEntity.getOrganisationsFromList(organisationUuids);

            matchingOrgs = baseOrganisationList.stream()
                    .filter(org -> org.getName().toLowerCase().contains(searchTerm.toLowerCase())
                            || org.getOdsCode().toLowerCase().contains(searchTerm.toLowerCase())).collect(Collectors.toList());
        }

        return Response
                .ok()
                .entity(matchingOrgs)
                .build();
    }

    private Response searchOrganisationsWithODSList(List<String> organisationUuids, List<String> odsCodes) throws Exception {
        List<OrganisationEntity> baseOrganisationList = new ArrayList<>();
        List<OrganisationEntity> matchingOrgs = new ArrayList<>();

        if (!organisationUuids.isEmpty()) {
            baseOrganisationList = OrganisationEntity.getOrganisationsFromList(organisationUuids);

            matchingOrgs = baseOrganisationList.stream()
                    .filter(org -> odsCodes.contains(org.getOdsCode()))
                            .collect(Collectors.toList());
        }

        return Response
                .ok()
                .entity(matchingOrgs)
                .build();
    }

    private Response searchOrganisationsInRegion(String regionUUID, String searchTerm, List<String> odsCodes) throws Exception {
        List<String> organisationUuids = MasterMappingEntity.getChildMappings(regionUUID, MapType.REGION.getMapType(), MapType.ORGANISATION.getMapType());

        if (searchTerm != null) {
            return searchOrganisations(organisationUuids, searchTerm);
        } else {
            return searchOrganisationsWithODSList(organisationUuids, odsCodes);
        }

    }

    private Response searchPublishersInDSA(String dsaUUID, String searchTerm, List<String> odsCodes) throws Exception {
        List<String> organisationUuids =  MasterMappingEntity.getChildMappings(dsaUUID, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());

        if (searchTerm != null) {
            return searchOrganisations(organisationUuids, searchTerm);
        } else {
            return searchOrganisationsWithODSList(organisationUuids, odsCodes);
        }
    }

    private Response searchSubscribersInDSA(String dsaUUID, String searchTerm, List<String> odsCodes) throws Exception {
        List<String> organisationUuids =  MasterMappingEntity.getChildMappings(dsaUUID, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.SUBSCRIBER.getMapType());

        if (searchTerm != null) {
            return searchOrganisations(organisationUuids, searchTerm);
        } else {
            return searchOrganisationsWithODSList(organisationUuids, odsCodes);
        }
    }

    private Response startUpload() throws Exception {
        bulkUploadMappings.clear();
        bulkOrgMap.clear();
        childParentMap.clear();
        uploadInProgress = true;

        return Response
                .ok()
                .build();
    }



    private Response endUpload() throws Exception {

        bulkUploadMappings.clear();
        bulkOrgMap.clear();
        childParentMap.clear();
        uploadInProgress = false;

        return Response
                .ok()
                .build();
    }

    private boolean processFile(String filename) throws Exception {
        if (filename.contains("earchive")
                || filename.contains("ecarehomesucc")
                || filename.contains("econcur")
                || filename.contains("edconcur")
                || filename.contains("educationtype")
                || filename.contains("egdp")
                || filename.contains("egdppracmem")
                || filename.contains("egpcur")
                || filename.contains("egpmema")
                || filename.contains("enurse")
                || filename.contains("epcdp")
                || filename.contains("epcdpam")
                || filename.contains("epcdpaq")
                || filename.contains("epcmem")
                || filename.contains("epracmem")
                || filename.contains("ngpcur")
                || filename.contains("nlhscgpr")
                || filename.contains("scotgp")
                || filename.contains("scotmem")
                || filename.contains("succ")
                || filename.contains("wconcur")
                || filename.contains("egparc"))
            return false;


        return true;
    }

    private Response processCSVFile(JsonFileUpload file) throws Exception {
        boolean found = false;
        int orgsUploaded = 0;
        //OrganisationEntity.deleteUneditedBulkOrganisations();

        List<OrganisationEntity> updatedBulkOrganisations = new ArrayList<>(); // OrganisationEntity.getUpdatedBulkOrganisations();

        List<OrganisationEntity> organisationEntities = new ArrayList<>();
        List<AddressEntity> addressEntities = new ArrayList<>();


        System.out.println(file.getName());

        if (!processFile(file.getName())){
            return Response
                    .ok(organisationEntities.size())
                    .build();
        }

        if (file.getFileData() == null) {
            throw new Exception("No File Data transferred");
        }

        String csvData = file.getFileData();

        try (Scanner scanner = new Scanner(csvData)) {

            int i = 0;
            while (scanner.hasNext()) {
                i++;
                List<String> org = CsvHelper.parseLine(scanner.nextLine());

                OrganisationEntity importedOrg = createOrganisationEntity(org, file.getName());

                for (OrganisationEntity oe : updatedBulkOrganisations) {
                    if (oe.getUuid().equals(importedOrg.getUuid())) {
                        found = true;
                    }
                }

                if (found) {
                    //already have this org and it has been updated so set the conflicted UUID to the UUID of the original org and generate a new UUID
                    importedOrg.setBulkConflictedWith(importedOrg.getUuid());
                    importedOrg.setUuid(UUID.nameUUIDFromBytes((importedOrg.getName() + importedOrg.getOdsCode() + "conflict").getBytes()).toString());
                }

                if (bulkOrgMap.get(importedOrg.getOdsCode()) == null) {
                    organisationEntities.add(importedOrg);
                    addressEntities.add(createAddressEntity(org, importedOrg.getUuid()));
                    bulkOrgMap.put(importedOrg.getOdsCode(), importedOrg.getUuid());
                }

                List<String> parents;
                if (!org.get(14).equals("")) {
                    parents = childParentMap.getOrDefault(importedOrg.getOdsCode(), new ArrayList<>());
                    if (!parents.contains(org.get(14))) {
                        parents.add(org.get(14));
                        childParentMap.put(importedOrg.getOdsCode(), parents);
                    }
                }

                if (!org.get(2).equals("")) {
                    parents = childParentMap.getOrDefault(importedOrg.getOdsCode(), new ArrayList<>());
                    if (!parents.contains(org.get(2))) {
                        parents.add(org.get(2));
                        childParentMap.put(importedOrg.getOdsCode(), parents);
                    }
                }

                if (!org.get(3).equals("") ) {
                    parents = childParentMap.getOrDefault(importedOrg.getOdsCode(), new ArrayList<>());
                    if (!parents.contains(org.get(3))) {
                        parents.add(org.get(3));
                        childParentMap.put(importedOrg.getOdsCode(), parents);
                    }
                }

                if (i % 200 == 0 ) {
                    orgsUploaded += organisationEntities.size();
                    OrganisationEntity.bulkSaveOrganisation(organisationEntities);
                    organisationEntities.clear();
                    AddressEntity.bulkSaveAddresses(addressEntities);
                    addressEntities.clear();
                }


                found = false;
            }

            orgsUploaded += organisationEntities.size();
            OrganisationEntity.bulkSaveOrganisation(organisationEntities);
            organisationEntities.clear();
            AddressEntity.bulkSaveAddresses(addressEntities);
            addressEntities.clear();
        }

        return Response
                .ok(orgsUploaded)
                .build();
    }

    private Response saveBulkMappings(Integer limit) throws Exception {

        Integer i = 0;
        for (Iterator<Map.Entry<String, List<String>>> it = childParentMap.entrySet().iterator(); it.hasNext();) {
            i++;
            Map.Entry<String, List<String>> map = it.next();

            for (String parent : map.getValue()) {

                MasterMappingEntity mappingEntity = new MasterMappingEntity();
                mappingEntity.setChildUuid(bulkOrgMap.get(map.getKey()));
                mappingEntity.setChildMapTypeId(MapType.ORGANISATION.getMapType());
                mappingEntity.setParentUuid(bulkOrgMap.get(parent));
                mappingEntity.setParentMapTypeId(MapType.ORGANISATION.getMapType());

                if (mappingEntity.getParentUuid() != null && mappingEntity.getChildUuid() != null)
                    bulkUploadMappings.add(mappingEntity);
            }

            if (i % 200 == 0 ) {
                MasterMappingEntity.bulkSaveMappings(bulkUploadMappings);
                bulkUploadMappings.clear();
            }

            it.remove();
            if (i > limit)
                break;
        }

        /*childParentMap.forEach((k, v) -> {
            MasterMappingEntity map = new MasterMappingEntity();
            map.setChildUuid(bulkOrgMap.get(k));
            map.setChildMapTypeId(MapType.ORGANISATION.getMapType());
            map.setParentUuid(bulkOrgMap.get(v));
            map.setParentMapTypeId(MapType.ORGANISATION.getMapType());

            if (map.getParentUuid() != null && map.getChildUuid() != null)
                bulkUploadMappings.add(map);
        });*/

        if (!bulkUploadMappings.isEmpty()) {
            MasterMappingEntity.bulkSaveMappings(bulkUploadMappings);
            bulkUploadMappings.clear();
        }

        return Response
                .ok(childParentMap.size())
                .build();
    }

    private byte getOrgTypeFromFilename(String filename, String odsCode) throws Exception {

        String file = filename.toLowerCase();

        if (file.contains("epraccur"))
            return OrganisationType.GPPRACTICE.getOrganisationType(); // "GP Practice";

        if (file.contains("etrust")) {
            if (odsCode.length() <= 3)
                return OrganisationType.NHSTRUST.getOrganisationType(); // "NHS Trust";
            else
                return OrganisationType.NHSTRUSTSITE.getOrganisationType(); // "NHS Trust Site";
        }

        if (file.contains("etr"))
            return OrganisationType.NHSTRUST.getOrganisationType(); // "NHS Trust";

        if (file.contains("plab"))
            return OrganisationType.PATHLAB.getOrganisationType(); // "Pathology Laboratories";

        if (file.contains("epracarc"))
            return OrganisationType.GPPRACTICE.getOrganisationType(); // "Archived GP Practice";

        if (file.contains("branch"))
            return OrganisationType.BRANCH.getOrganisationType(); // "Branch";

        if (file.contains("auth"))
            return OrganisationType.COMMISSIONINGREGION.getOrganisationType(); // "Commissioning Region";

        if (file.contains("ecare") && odsCode.length() > 3)
            return OrganisationType.CARETRUSTSITE.getOrganisationType(); // "Care Trust Site";

        if (file.contains("ecare") && odsCode.length() <= 3)
            return OrganisationType.CARETRUST.getOrganisationType(); // "Care Trust";

        if (file.contains("ccgsite"))
            return OrganisationType.CCGSITE.getOrganisationType(); // "CCG Site";

        if (file.contains("ccg"))
            return OrganisationType.CCG.getOrganisationType(); // "CCG";

        if (file.contains("csuaq"))
            return OrganisationType.CSU.getOrganisationType(); // "CSU";

        if (file.contains("csusite"))
            return OrganisationType.CSUSITE.getOrganisationType(); // "CSU Site";

        if (file.equals("ect.csv"))
            return OrganisationType.CARETRUST.getOrganisationType(); // "Care Trust";

        if (file.contains("ectsite"))
            return OrganisationType.CARETRUSTSITE.getOrganisationType(); // "Care Trust Site";

        if (file.contains("dispensary"))
            return OrganisationType.DISPENSARY.getOrganisationType(); // "Dispensary";

        if (file.contains("educate"))
            return OrganisationType.EDUCATION.getOrganisationType(); // "Education Establishment";

        if (file.contains("egp"))
            return OrganisationType.GPPRACTICE.getOrganisationType(); // "GP Practice";

        if (file.contains("hospice")) {
            if (odsCode.length() > 3)
                return OrganisationType.NONNHSHOSPICE.getOrganisationType(); // "Non NHS Hospice";
            if (odsCode.length() <= 3)
                return OrganisationType.NHSHOSPICE.getOrganisationType(); // "NHS Hospice";
        }

        if (file.contains("iom")) {
            if (odsCode.length() <= 3) {
                if (odsCode.substring(1, 1).equals("K"))
                    return OrganisationType.IOMGOVDIRECTORATE.getOrganisationType(); // "IoM Government Directorate";
                else
                    return OrganisationType.IOMGOVDEPT.getOrganisationType(); // "IoM Government Department";
            } else
                return OrganisationType.IOMGOVDIRECTORATESITE.getOrganisationType(); // "IoM Government Directorate Site";

        }

        if (file.contains("justice"))
            return OrganisationType.JUSTICEENTITY.getOrganisationType(); // "Justice Entity";

        if (file.contains("nonnhs"))
            return OrganisationType.NONNHSORGANISATION.getOrganisationType(); // "Non NHS Organisation";

        if (file.equals("ensa.csv"))
            return OrganisationType.NHSSUPPORTAGENGY.getOrganisationType(); // "NHS Support Agency and Shared Service";

        if (file.contains("eopthq"))
            return OrganisationType.OPTICALHQ.getOrganisationType(); // "Optical Headquarters";

        if (file.contains("eoptsite"))
            return OrganisationType.OPTICALSITE.getOrganisationType(); // "Optical Site";

        if (file.contains("other"))
            return OrganisationType.OTHER.getOrganisationType(); // "Other";

        if (file.contains("pharmacyhq"))
            return OrganisationType.PHARMACYHQ.getOrganisationType(); // "Pharmacy Headquarters";

        if (file.contains("ephpsite"))
            return OrganisationType.ISHPSITE.getOrganisationType(); // "ISHP Site";

        if (file.contains("ephp"))
            return OrganisationType.ISHP.getOrganisationType(); // "ISHP";

        if (file.contains("prison"))
            return OrganisationType.PRISON.getOrganisationType(); // "Prison";

        if (file.contains("school"))
            return OrganisationType.SCHOOL.getOrganisationType(); // "School";

        if (file.contains("spha"))
            return OrganisationType.SPECIALHEALTH.getOrganisationType(); // "Special Health Authority";

        if (file.contains("lauthsite"))
            return OrganisationType.LOCALAUTHORITYSITE.getOrganisationType(); // "Local Authority Site";

        if (file.contains("lauth"))
            return OrganisationType.LOCALAUTHORITY.getOrganisationType(); // "Local Authority";

        if (file.contains("niarchive") || file.contains("niorg"))
            return OrganisationType.NIORG.getOrganisationType(); // "NI Organisation";

        if (file.contains("scotgp"))
            return OrganisationType.SCOTTISHGP.getOrganisationType(); // "Scottish GP Practice";

        if (file.contains("scotorg"))
            return OrganisationType.SCOTTISHPROVIDER.getOrganisationType(); // "Scottish Provider Organisation";

        if (file.contains("whbs")) {
            if (odsCode.length() > 3)
                return OrganisationType.WALESHBSITE.getOrganisationType(); // "Wales Health Board Site";
            else
                return OrganisationType.WALESHB.getOrganisationType(); // "Wales Health Board";
        }

        return OrganisationType.OTHER.getOrganisationType(); // "Unknown";
    }

    private OrganisationEntity createOrganisationEntity(List<String> org, String filename) throws Exception {

        OrganisationEntity organisationEntity = new OrganisationEntity();
        organisationEntity.setName(org.get(1));
        organisationEntity.setOdsCode(org.get(0));
        organisationEntity.setUuid(UUID.nameUUIDFromBytes((organisationEntity.getName() + organisationEntity.getOdsCode()).getBytes()).toString());

        organisationEntity.setIsService((byte)0);
        organisationEntity.setBulkImported((byte)1);
        organisationEntity.setBulkItemUpdated((byte)0);
        organisationEntity.setType(getOrgTypeFromFilename(filename, org.get(0)));
        if (filename.equals("epracarc"))
            organisationEntity.setActive((byte)0);
        else
            organisationEntity.setActive((byte)1);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date = format.parse(org.get(10));

        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        organisationEntity.setDateOfRegistration(java.sql.Date.valueOf(localDate));

        return organisationEntity;
    }

    private AddressEntity createAddressEntity(List<String> org, String organisationUuid) throws Exception {

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setOrganisationUuid(organisationUuid);

        addressEntity.setBuildingName(org.get(4));
        addressEntity.setNumberAndStreet(org.get(5));
        addressEntity.setLocality(org.get(6));
        addressEntity.setCity(org.get(7));
        addressEntity.setCounty(org.get(8));
        addressEntity.setPostcode(org.get(9));

        return addressEntity;
    }

}
