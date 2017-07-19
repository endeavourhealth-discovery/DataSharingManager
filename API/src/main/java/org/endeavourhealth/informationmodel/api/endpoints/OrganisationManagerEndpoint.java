package org.endeavourhealth.informationmodel.api.endpoints;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.mySQLDatabase.MapType;
import org.endeavourhealth.informationmodel.api.database.models.AddressEntity;
import org.endeavourhealth.informationmodel.api.database.models.MasterMappingEntity;
import org.endeavourhealth.informationmodel.api.database.models.OrganisationEntity;
import org.endeavourhealth.informationmodel.api.database.models.RegionEntity;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.JsonAddress;
import org.endeavourhealth.coreui.json.JsonMarker;
import org.endeavourhealth.informationmodel.api.json.JsonOrganisationManager;
import org.endeavourhealth.informationmodel.api.json.JsonOrganisationManagerStatistics;
import org.endeavourhealth.informationmodel.api.metrics.InformationManagerMetricListener;
import org.endeavourhealth.informationmodel.api.json.JsonFileUpload;
import org.endeavourhealth.informationmodel.api.utility.CsvHelper;
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

@Path("/organisationManager")
@Metrics(registry = "EdsRegistry")
public final class OrganisationManagerEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationManagerEndpoint.class);

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
    @Timed(absolute = true, name="EDS-UI.OrganisationManagerEndpoint.Get")
    @Path("/")
    public Response get(@Context SecurityContext sc,
                            @QueryParam("uuid") String uuid,
                            @QueryParam("searchData") String searchData,
                            @QueryParam("searchType") String searchType,
                            @QueryParam("pageNumber") Integer pageNumber,
                            @QueryParam("pageSize") Integer pageSize,
                            @QueryParam("orderColumn") String orderColumn,
                            @QueryParam("descending") boolean descending) throws Exception {

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
    @Timed(absolute = true, name="EDS-UI.OrganisationManagerEndpoint.Post")
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonOrganisationManager organisationManager) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", organisationManager);

        if (organisationManager.getUuid() != null) {
            MasterMappingEntity.deleteAllMappings(organisationManager.getUuid());
            OrganisationEntity.updateOrganisation(organisationManager);
        } else {
            organisationManager.setUuid(UUID.nameUUIDFromBytes((organisationManager.getName() + organisationManager.getOdsCode()).getBytes()).toString());
            OrganisationEntity.saveOrganisation(organisationManager);
        }


        //Process Mappings
        MasterMappingEntity.saveOrganisationMappings(organisationManager);

        List<JsonAddress> addresses = organisationManager.getAddresses();
        if (addresses.size() > 0) {
            for (JsonAddress address : addresses) {
                if (address.getOrganisationUuid() == null)
                    address.setOrganisationUuid(organisationManager.getUuid());

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
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.OrganisationManagerEndpoint.Delete")
    @Path("/")
    @RequiresAdmin
    public Response deleteOrganisation(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
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
    @Timed(absolute = true, name="EDS-UI.OrganisationManagerEndpoint.GetRegions")
    @Path("/regions")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Region(s)",
                "Organisation Id", uuid);

        return getRegionsForOrganisation(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.OrganisationManagerEndpoint.GetChildOrganisations")
    @Path("/childOrganisations")
    public Response getChildOrganisations(@Context SecurityContext sc,
                                          @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Child Organisations(s)",
                "Organisation Id", uuid);


        return getChildOrganisations(uuid, MapType.ORGANISATION.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.OrganisationManagerEndpoint.GetServices")
    @Path("/services")
    public Response getServices(@Context SecurityContext sc,
                                @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "services(s)",
                "Organisation Id", uuid);

        return getChildOrganisations(uuid, MapType.SERVICE.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.OrganisationManagerEndpoint.GetParentOrganisations")
    @Path("/parentOrganisations")
    public Response getParentOrganisations(@Context SecurityContext sc,
                                           @QueryParam("uuid") String uuid,
                                           @QueryParam("isService") String isService) throws Exception {
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
    @Timed(absolute = true, name="OrganisationManager.GetAddresses")
    @Path("/addresses")
    public Response getAddresses(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Address(es)",
                "Organisation Id", uuid);

        return getOrganisationAddressList(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetMarkers")
    @Path("/markers")
    public Response getMarkers(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Marker(s)",
                "Region Id", uuid);

        return getOrganisationMarkers(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetEditedBulks")
    @Path("/editedBulks")
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
    @Timed(absolute = true, name="OrganisationManager.GetConflicts")
    @Path("/conflicts")
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
    @Timed(absolute = true, name="OrganisationManager.GetOrganisationStatistics")
    @Path("/organisationStatistics")
    @RequiresAdmin
    public Response getOrganisationsStatistics(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation Statistics",
                "Organisation", null);

        LOG.trace("Statistics obtained");
        return generateStatistics(OrganisationEntity.getStatistics("getOrganisationStatistics"));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetServiceStatistics")
    @Path("/serviceStatistics")
    @RequiresAdmin
    public Response getServiceStatistics(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Service Statistics",
                "Organisation", null);

       return generateStatistics(OrganisationEntity.getStatistics("getServiceStatistics"));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetRegionStatistics")
    @Path("/regionStatistics")
    @RequiresAdmin
    public Response getRegionStatistics(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Service Statistics",
                "Organisation", null);

        return generateStatistics(OrganisationEntity.getStatistics("getRegionStatistics"));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.DeleteBulks")
    @Path("/deleteBulks")
    @RequiresAdmin
    public Response deleteBulks(@Context SecurityContext sc) throws Exception {
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
    @Timed(absolute = true, name="OrganisationManager.UploadBulkOrganisations")
    @Path("/upload")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonFileUpload fileUpload) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", fileUpload);

        return processCSVFile(fileUpload);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.startUpload")
    @Path("/startUpload")
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
    @Timed(absolute = true, name="OrganisationManager.endUpload")
    @Path("/endUpload")
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
    @Timed(absolute = true, name="InformationManager.ConceptEndpoint.searchCount")
    @Path("/searchCount")
    public Response getConceptSearchCount(@Context SecurityContext sc,
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
        Iterable<OrganisationEntity> organisations = OrganisationEntity.getOrganisations(searchData, searchServices,
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

    private Response generateStatistics(List<Object []> statistics) throws Exception {

        List<JsonOrganisationManagerStatistics> ret = new ArrayList<>();

        for (Object[] stat : statistics) {
            JsonOrganisationManagerStatistics jsonStat = new JsonOrganisationManagerStatistics();
            jsonStat.setLabel(stat[0].toString());
            jsonStat.setValue(stat[1].toString());

            ret.add(jsonStat);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getOrganisationMarkers(String regionUuid) throws Exception {

        List<Object[]> markers = AddressEntity.getOrganisationsMarkers(regionUuid);

        List<JsonMarker> ret = new ArrayList<>();

        for (Object[] marker : markers) {
            String name = marker[0].toString();
            Double lat = marker[1]==null?0.0:Double.parseDouble(marker[1].toString());
            Double lng = marker[2]==null?0.0:Double.parseDouble(marker[2].toString());

            JsonMarker jsonMarker = new JsonMarker();
            jsonMarker.setName(name);
            jsonMarker.setLat(lat);
            jsonMarker.setLng(lng);

            ret.add(jsonMarker);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
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
            map.setParentUUid(bulkOrgMap.get(v));
            map.setParentMapTypeId(MapType.ORGANISATION.getMapType());

            if (map.getParentUUid() != null)
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
