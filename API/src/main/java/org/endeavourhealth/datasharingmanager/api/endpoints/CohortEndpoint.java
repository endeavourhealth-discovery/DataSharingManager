package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.MasterMappingDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonCohort;
import org.endeavourhealth.core.database.dal.usermanager.caching.*;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.*;
import org.endeavourhealth.core.database.rdbms.usermanager.models.UserRegionEntity;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.DAL.CohortDAL;
import org.endeavourhealth.datasharingmanager.api.Logic.RegionLogic;
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

@Path("/cohort")
@Api(description = "API endpoint related to the Cohorts")
public final class CohortEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(CohortEndpoint.class);
    private static final String COHORT_ID = "Cohort Id";
    private static final String COHORT = "Cohort";

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);
    private static MasterMappingDalI masterMappingRepository = DalProvider.factoryDSMMasterMappingDal();


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.CohortEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return either all cohorts if no parameter is provided or search for " +
            "cohorts using a UUID or a search term. Search matches on name of cohorts. " +
            "Returns a JSON representation of the matching set of cohorts")
    public Response getCohort(@Context SecurityContext sc,
                              @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                              @ApiParam(value = "Optional userId") @QueryParam("userId") String userId,
                              @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Cohort(s)",
                COHORT_ID, uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getCohort - list");

            return getCohortList(userId);
        } else if (uuid != null){
            LOG.trace("getCohort - single - {}", uuid);
            return getSingleCohort(uuid);
        } else {
            LOG.trace("Search Cohort - {}", searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.CohortEndpoint.Post")
    @Path("/")
    @ApiOperation(value = "Save a new cohort or update an existing one.  Accepts a JSON representation " +
            "of a cohort.")
    @RequiresAdmin
    public Response postCohort(@Context SecurityContext sc,
                               @HeaderParam("userProjectId") String userProjectId,
                               @ApiParam(value = "Json representation of cohort to save or update") JsonCohort cohort
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                COHORT,
                "Cohort", cohort);

        if (cohort.getUuid() != null) {
            new CohortDAL().updateCohort(cohort, userProjectId, false);
        } else {
            cohort.setUuid(UUID.randomUUID().toString());
            new CohortDAL().saveCohort(cohort, userProjectId);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(cohort.getUuid())
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.CohortEndpoint.Post")
    @Path("/updateMappings")
    @ApiOperation(value = "Updates the DPA mapping.  Accepts a JSON representation of a cohort.")
    @RequiresAdmin
    public Response updateMappings(@Context SecurityContext sc,
                               @HeaderParam("userProjectId") String userProjectId,
                               @ApiParam(value = "Json representation of cohort to save or update") JsonCohort cohort
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                COHORT,
                "Cohort", cohort);

        new CohortDAL().updateCohort(cohort, userProjectId, true);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(cohort.getUuid())
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.CohortEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete a cohort based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteCohort(@Context SecurityContext sc,
                                 @HeaderParam("userProjectId") String userProjectId,
                                 @ApiParam(value = "UUID of the cohorts to be deleted") @QueryParam("uuids") List<String> uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                COHORT,
                COHORT_ID, uuids);

        for (String uuid : uuids) {
            new CohortDAL().deleteCohort(uuid, userProjectId);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.CohortEndpoint.GetDataProcessingAgreements")
    @Path("/dpas")
    @ApiOperation(value = "Returns a list of Json representations of Data Processing Agreements that are linked " +
            "to the cohort.  Accepts a UUID of a cohort.")
    public Response getDpaForCohort(@Context SecurityContext sc,
                                    @ApiParam(value = "UUID of cohort") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                COHORT_ID, uuid);

        return getLinkedDpas(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.CohortEndpoint.GetDataSharingAgreements")
    @Path("/dsas")
    @ApiOperation(value = "Returns a list of Json representations of Data Sharing Agreements that are linked " +
            "to the cohort.  Accepts a UUID of a cohort.")
    public Response getDsaForCohort(@Context SecurityContext sc,
                                    @ApiParam(value = "UUID of cohort") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DSA(s)",
                COHORT_ID, uuid);

        return getLinkedDsas(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.CohortEndpoint.GetProjects")
    @Path("/projects")
    @ApiOperation(value = "Returns a list of Json representations of Data Sharing Agreements that are linked " +
            "to the cohort.  Accepts a UUID of a cohort.")
    public Response getProjectsForCohort(@Context SecurityContext sc,
                                    @ApiParam(value = "UUID of cohort") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Projects(s)",
                COHORT_ID, uuid);

        return getLinkedProjects(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.CohortEndpoint.GetRegions")
    @Path("/regions")
    @ApiOperation(value = "Returns a list of Json representations of regions that are linked " +
            "to the data processing agreement.  Accepts a UUID of a cohort.")
    public Response getLinkedRegionsForDataSet(@Context SecurityContext sc,
                                               @ApiParam(value = "UUID of cohort") @QueryParam("uuid") String uuid,
                                               @ApiParam(value = "Optional user Id to restrict based on region") @QueryParam("userId") String userId
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Region(s)",
                COHORT_ID, uuid);

        return getLinkedRegions(uuid, userId);
    }

    private Response getCohortList(String userId) throws Exception {

        List<CohortEntity> cohorts = new CohortDAL().getAllCohorts();
        List<CohortEntity> cohortsBasedOnRegion = new ArrayList<>();
        if (StringUtils.isNotEmpty(userId)) {
            for (CohortEntity cohort : cohorts) {
                List<String> regionUuids = masterMappingRepository.getParentMappings(cohort.getUuid(),
                        MapType.COHORT.getMapType(), MapType.REGION.getMapType());
                UserRegionEntity userRegion = UserCache.getUserRegion(userId);
                if (regionUuids.contains(userRegion.getRegionId())) {
                    cohortsBasedOnRegion.add(cohort);
                }
            }
            cohorts = cohortsBasedOnRegion;
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(cohorts)
                .build();
    }

    private Response getSingleCohort(String uuid) throws Exception {
        CohortEntity cohortEntity = CohortCache.getCohortDetails(uuid);

        return Response
                .ok()
                .entity(cohortEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<CohortEntity> cohorts = new CohortDAL().search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(cohorts)
                .build();
    }

    private Response getLinkedDpas(String cohortUuid) throws Exception {

        List<String> dpaUuids = masterMappingRepository.getParentMappings(cohortUuid, MapType.COHORT.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());

        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (!dpaUuids.isEmpty())
            ret = DataProcessingAgreementCache.getDPADetails(dpaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedDsas(String cohortUuid) throws Exception {

        List<String> dsaUuids = masterMappingRepository.getParentMappings(cohortUuid, MapType.COHORT.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());

        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUuids.isEmpty())
            ret = DataSharingAgreementCache.getDSADetails(dsaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedProjects(String cohortUuid) throws Exception {

        List<String> dsaUuids = masterMappingRepository.getParentMappings(cohortUuid, MapType.COHORT.getMapType(), MapType.PROJECT.getMapType());

        List<ProjectEntity> ret = new ArrayList<>();

        if (!dsaUuids.isEmpty())
            ret = ProjectCache.getProjectDetails(dsaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedRegions(String cohortUid, String userId) throws Exception {

        List<String> regionUuids = masterMappingRepository.getParentMappings(cohortUid, MapType.COHORT.getMapType(), MapType.REGION.getMapType());

        if (userId != null) {
            regionUuids = new RegionLogic().filterRegionsForUser(regionUuids, userId);
        }

        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = RegionCache.getRegionDetails(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}

