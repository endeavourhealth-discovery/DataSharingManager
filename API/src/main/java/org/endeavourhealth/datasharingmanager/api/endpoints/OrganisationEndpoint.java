package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.datasharingmanager.api.database.MapType;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.database.models.*;
import org.endeavourhealth.datasharingmanager.api.json.*;
import org.endeavourhealth.datasharingmanager.api.metrics.InformationManagerMetricListener;
import org.endeavourhealth.datasharingmanager.api.utility.CsvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Path("/organisation")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to the organisations and services.  " +
        "Services are just organisations with a flag indicating they are a service.")
public final class OrganisationEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static List<MasterMappingEntity> bulkUploadMappings = new ArrayList<>();
    private static HashMap<String, String> bulkOrgMap = new HashMap<>();
    private static HashMap<String, String> childParentMap = new HashMap<>();
    private static boolean uploadInProgress = false;

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);
    private static final MetricRegistry metricRegistry = InformationManagerMetricListener.informationManagerMetricRegistry;

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
        } else {
            LOG.trace("Search Organisations - " + searchData + searchType);
            return getOrganisations(searchData, searchServices, pageNumber, pageSize, orderColumn, descending);
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
            organisation.setUuid(UUID.nameUUIDFromBytes((organisation.getName() + organisation.getOdsCode()).getBytes()).toString());
            OrganisationEntity.saveOrganisation(organisation);
        }


        //Process Mappings
        MasterMappingEntity.saveOrganisationMappings(organisation);

        List<JsonAddress> addresses = organisation.getAddresses();
        if (addresses.size() > 0) {
            for (JsonAddress address : addresses) {
                if (address.getOrganisationUuid() == null)
                    address.setOrganisationUuid(organisation.getUuid());

                if (address.getUuid() == null) {
                    address.setUuid(UUID.randomUUID().toString());
                    AddressEntity.saveAddress(address);
                }
                else
                    AddressEntity.updateAddress(address);

                getGeolocation(address);
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
                "Organisation", fileUpload);

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
                                          @QueryParam("expression") String expression,
                                          @QueryParam("searchType") String searchType
    ) throws Exception {

        boolean searchServices = false;
        if (searchType != null && searchType.equals("services"))
            searchServices = true;

        if (expression == null)
            expression = "";

        return getTotalNumberOfOrganisations(expression, searchServices);
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

    private Response getOrganisations(String searchData, boolean searchServices,
                            Integer pageNumber, Integer pageSize,
                            String orderColumn, boolean descending) throws Exception {
        List<OrganisationEntity> organisations = OrganisationEntity.getOrganisations(searchData, searchServices,
                pageNumber, pageSize, orderColumn, descending);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    private Response getRegionsForOrganisation(String organisationUuid) throws Exception {

        List<String> regionUuids = MasterMappingEntity.getParentMappings(organisationUuid, MapType.ORGANISATION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (regionUuids.size() > 0)
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

        if (dpaUUIDs.size() > 0)
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

        if (dsaUuids.size() > 0)
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

        if (dsaUUIds.size() > 0)
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

    private void getGeolocation(JsonAddress address) throws Exception {
        Client client = ClientBuilder.newClient();

        JsonNode json = ConfigManager.getConfigurationAsJson("GoogleMapsAPI");
        String url = json.get("url").asText();
        String apiKey = json.get("apiKey").asText();

        WebTarget resource = client.target(url + address.getPostcode().replace(" ", "+") + "&key=" + apiKey);

        Invocation.Builder request = resource.request();
        request.accept(MediaType.APPLICATION_JSON_TYPE);

        Response response = request.get();

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            String s = response.readEntity(String.class);
            JsonParser parser = new JsonParser();
            JsonElement obj = parser.parse(s);
            JsonObject jo = obj.getAsJsonObject();
            JsonElement results = jo.getAsJsonArray("results").get(0);
            JsonObject location = results.getAsJsonObject().getAsJsonObject("geometry").getAsJsonObject("location");

            address.setLat(Double.parseDouble(location.get("lat").toString()));
            address.setLng(Double.parseDouble(location.get("lng").toString()));

            AddressEntity.updateGeolocation(address);
        }


    }

    private Response getChildOrganisations(String organisationUuid, Short organisationType) throws Exception {

        List<String> organisationUuids = MasterMappingEntity.getChildMappings(organisationUuid, MapType.ORGANISATION.getMapType(), organisationType);
        List<OrganisationEntity> ret = new ArrayList<>();

        if (organisationUuids.size() > 0)
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

        if (organisationUuids.size() > 0)
            ret = OrganisationEntity.getOrganisationsFromList(organisationUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
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
        saveBulkMappings();

        bulkUploadMappings.clear();
        bulkOrgMap.clear();
        childParentMap.clear();
        uploadInProgress = false;

        return Response
                .ok()
                .build();
    }

    private Response processCSVFile(JsonFileUpload file) throws Exception {
        boolean found = false;
        //OrganisationEntity.deleteUneditedBulkOrganisations();

        List<OrganisationEntity> updatedBulkOrganisations = OrganisationEntity.getUpdatedBulkOrganisations();

        List<OrganisationEntity> organisationEntities = new ArrayList<>();
        List<AddressEntity> addressEntities = new ArrayList<>();


        System.out.println(file.getName());
        if (file.getFileData() == null) {
            throw new Exception("No File Data transferred");
        }

        String csvData = file.getFileData();
        Scanner scanner = new Scanner(csvData);

        while (scanner.hasNext()) {
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

            if (!org.get(14).equals("") && childParentMap.get(importedOrg.getOdsCode()) == null) {
                childParentMap.put(importedOrg.getOdsCode(), org.get(14));
            }


            found = false;
        }

        OrganisationEntity.bulkSaveOrganisation(organisationEntities);
        AddressEntity.bulkSaveAddresses(addressEntities);

        return Response
                .ok()
                .build();
    }

    private void saveBulkMappings() throws Exception {

        childParentMap.forEach((k, v) -> {
            MasterMappingEntity map = new MasterMappingEntity();
            map.setChildUuid(bulkOrgMap.get(k));
            map.setChildMapTypeId(MapType.ORGANISATION.getMapType());
            map.setParentUuid(bulkOrgMap.get(v));
            map.setParentMapTypeId(MapType.ORGANISATION.getMapType());

            if (map.getParentUuid() != null)
                bulkUploadMappings.add(map);
        });

        if (bulkUploadMappings.size() > 0)
            MasterMappingEntity.bulkSaveMappings(bulkUploadMappings);
    }

    private String getOrgTypeFromFilename(String filename, String odsCode) throws Exception {

        String file = filename.toLowerCase();

        if (file.contains("epraccur"))
            return "GP Practice";

        if (file.contains("etrust")) {
            if (odsCode.length() <= 3)
                return "NHS Trust";
            else
                return "NHS Trust Site";
        }

        if (file.contains("etr"))
            return "NHS Trust";

        if (file.contains("plab"))
            return "Pathology Laboratories";

        if (file.contains("epracarc"))
            return "Archived GP Practice";

        if (file.contains("branch"))
            return "Branch";

        if (file.contains("auth"))
            return "Commissioning Region";

        if (file.contains("ecare") && odsCode.length() > 3)
            return "Care Trust Site";

        if (file.contains("ecare") && odsCode.length() <= 3)
            return "Care Trust";

        if (file.contains("ccgsite"))
            return "CCG Site";

        if (file.contains("ccg"))
            return "CCG";

        if (file.contains("ccgsite"))
            return "CCG Site";

        if (file.contains("csuaq"))
            return "CSU";

        if (file.contains("csusite"))
            return "CSU Site";

        if (file.equals("ect.csv"))
            return "Care Trust";

        if (file.contains("ectsite"))
            return "Care Trust Site";

        if (file.contains("dispensary"))
            return "Dispensary";

        if (file.contains("educate"))
            return "Education Establishment";

        if (file.contains("egp"))
            return "GP Practice";

        if (file.contains("hospice")) {
            if (odsCode.length() > 3)
                return "Non NHS Hospice";
            if (odsCode.length() <= 3)
                return "NHS Hospice";
        }

        if (file.contains("iom")) {
            if (odsCode.length() <= 3) {
                if (odsCode.substring(1, 1).equals("K"))
                    return "IoM Government Directorate";
                else
                    return "IoM Government Department";
            } else
                return "IoM Government Directorate Site";

        }

        if (file.contains("justice"))
            return "Justice Entity";

        if (file.contains("nonnhs"))
            return "Non NHS Organisation";

        if (file.equals("ensa.csv"))
            return "NHS Support Agency and Shared Service";

        if (file.contains("eopthq"))
            return "Optical Headquarters";

        if (file.contains("eoptsite"))
            return "Optical Site";

        if (file.contains("other"))
            return "Other";

        if (file.contains("pharmacyhq"))
            return "Pharmacy Headquarters";

        if (file.contains("ephpsite"))
            return "ISHP Site";

        if (file.contains("ephp"))
            return "ISHP";

        if (file.contains("prison"))
            return "Prison";

        if (file.contains("school"))
            return "School";

        if (file.contains("spha"))
            return "Special Health Authority";

        if (file.contains("lauthsite"))
            return "Local Authority Site";

        if (file.contains("lauth"))
            return "Local Authority";

        if (file.contains("niarchive") || file.contains("niorg"))
            return "NI Organisation";

        if (file.contains("scotgp"))
            return "Scottish GP Practice";

        if (file.contains("scotorg"))
            return "Scottish Provider Organisation";

        if (file.contains("whbs")) {
            if (odsCode.length() > 3)
                return "Wales Health Board Site";
            else
                return "Wales Health Board";
        }

        return "Unknown";
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
