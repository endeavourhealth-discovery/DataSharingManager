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
import org.endeavourhealth.datasharingmanager.api.database.MapType;
import org.endeavourhealth.datasharingmanager.api.database.models.CohortEntity;
import org.endeavourhealth.datasharingmanager.api.database.models.DataProcessingAgreementEntity;
import org.endeavourhealth.datasharingmanager.api.database.models.MasterMappingEntity;
import org.endeavourhealth.datasharingmanager.api.json.JsonCohort;
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
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to the Cohorts")
public final class CohortEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


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
                              @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Cohort(s)",
                "Cohort Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getCohort - list");

            return getCohortList();
        } else if (uuid != null){
            LOG.trace("getCohort - single - " + uuid);
            return getSingleCohort(uuid);
        } else {
            LOG.trace("Search Cohort - " + searchData);
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
                               @ApiParam(value = "Json representation of cohort to save or update") JsonCohort cohort
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Cohort",
                "Cohort", cohort);

        if (cohort.getUuid() != null) {
            MasterMappingEntity.deleteAllMappings(cohort.getUuid());
            CohortEntity.updateCohort(cohort);
        } else {
            cohort.setUuid(UUID.randomUUID().toString());
            CohortEntity.saveCohort(cohort);
        }

        MasterMappingEntity.saveCohortMappings(cohort);

        clearLogbackMarkers();

        return Response
                .ok()
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
                                 @ApiParam(value = "UUID of the cohort to be deleted") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Cohort",
                "Cohort Id", uuid);

        CohortEntity.deleteCohort(uuid);

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
                "Cohort Id", uuid);

        return getLinkedDpas(uuid);
    }

    private Response getCohortList() throws Exception {

        List<CohortEntity> cohorts = CohortEntity.getAllCohorts();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(cohorts)
                .build();
    }

    private Response getSingleCohort(String uuid) throws Exception {
        CohortEntity cohortEntity = CohortEntity.getCohort(uuid);

        return Response
                .ok()
                .entity(cohortEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<CohortEntity> cohorts = CohortEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(cohorts)
                .build();
    }

    private Response getLinkedDpas(String cohortUuid) throws Exception {

        List<String> dpaUuids = MasterMappingEntity.getParentMappings(cohortUuid, MapType.COHORT.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());

        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (dpaUuids.size() > 0)
            ret = DataProcessingAgreementEntity.getDPAsFromList(dpaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
