package org.endeavourhealth.informationmodel.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.mySQLDatabase.MapType;
import org.endeavourhealth.core.mySQLDatabase.models.*;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.JsonDSA;
import org.endeavourhealth.coreui.json.JsonPurpose;
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
public final class DsaEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DsaEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DsaEndpoint.Get")
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
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
    @Timed(absolute = true, name="EDS-UI.DsaEndpoint.Post")
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonDSA dsa) throws Exception {
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

        dsa.setPurposes(setUuidsAndSavePurpose(dsa.getPurposes()));
        dsa.setBenefits(setUuidsAndSavePurpose(dsa.getBenefits()));

        MasterMappingEntity.saveDataSharingAgreementMappings(dsa);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DsaEndpoint.Delete")
    @Path("/")
    @RequiresAdmin
    public Response delete(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
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
    @Timed(absolute = true, name="EDS-UI.DsaEndpoint.GetDataFlows")
    @Path("/dataflows")
    public Response getLinkedCohorts(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "dataflow(s)",
                "DSA Id", uuid);

        return getLinkedDataFlows(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DsaEndpoint.GetRegions")
    @Path("/regions")
    public Response getLinkedRegions(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "dataflow(s)",
                "DSA Id", uuid);

        return getLinkedRegions(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DsaEndpoint.GetPublishers")
    @Path("/publishers")
    public Response getPublishers(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "publisher(s)",
                "DSA Id", uuid);

        return getPublishers(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DsaEndpoint.GetSubscribers")
    @Path("/subscribers")
    public Response getSubscribers(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "subscriber(s)",
                "DSA Id", uuid);

        return getSubscribers(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DsaEndpoint.GetPurposes")
    @Path("/purposes")
    public Response getPurposes(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "purpose(s)",
                "DSA Id", uuid);

        return getPurposes(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DsaEndpoint.getBenefits")
    @Path("/benefits")
    public Response getBenefits(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "benefits(s)",
                "DSA Id", uuid);

        return getBenefits(uuid);
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

    private List<JsonPurpose> setUuidsAndSavePurpose(List<JsonPurpose> purposes) throws Exception {
        for (JsonPurpose purpose : purposes) {
            if (purpose.getUuid() == null) {
                purpose.setUuid(UUID.randomUUID().toString());
            }
            PurposeEntity.savePurpose(purpose);
        }

        return purposes;
    }

}
