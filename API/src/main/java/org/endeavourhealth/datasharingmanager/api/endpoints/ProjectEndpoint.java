package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectApplicationPolicyDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ProjectApplicationPolicyEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonAuthorityToShare;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonProject;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonProjectApplicationPolicy;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.ApplicationPolicyCache;
import org.endeavourhealth.common.security.usermanagermodel.models.database.ApplicationPolicyEntity;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.DAL.ProjectApplicationPolicyDAL;
import org.endeavourhealth.datasharingmanager.api.DAL.ProjectDAL;
import org.endeavourhealth.datasharingmanager.api.Logic.ProjectLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static org.endeavourhealth.common.security.SecurityUtils.getCurrentUserId;

@Path("/project")
@Api(description = "API endpoint related to the projects")
public class ProjectEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getProjects")
    @Path("/")
    @ApiOperation(value = "Return either all projects if no parameter is provided or search for " +
            "projects using a UUID or a search term. Search matches on name of projects. " +
            "Returns a JSON representation of the matching set of projects")
    public Response getProjects(@Context SecurityContext sc,
                                @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                                @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData,
                                @ApiParam(value = "Optional user Id to restrict based on region") @QueryParam("userId") String userId
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Project(s)",
                "Project UUID", uuid,
                "SearchData", searchData);

        return new ProjectLogic().getProjects(uuid, searchData, userId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.postProject")
    @Path("/")
    @ApiOperation(value = "Save a new project or update an existing one.  Accepts a JSON representation " +
            "of a project.")
    @RequiresAdmin
    public Response postProject(@Context SecurityContext sc,
                                @HeaderParam("userProjectId") String userProjectId,
                                @ApiParam(value = "Json representation of project to save or update") JsonProject project
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Project",
                "Project", project);

        clearLogbackMarkers();

        return new ProjectLogic().postProject(project, userProjectId);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.deleteProject")
    @Path("/")
    @ApiOperation(value = "Delete a project based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteProject(@Context SecurityContext sc,
                                  @HeaderParam("userProjectId") String userProjectId,
                                  @ApiParam(value = "UUID of the projects to be deleted") @QueryParam("uuids") List<String> uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Project",
                "Project UUID", uuids);

        for (String uuid : uuids) {
            new ProjectDAL().deleteProject(uuid, userProjectId);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getLinkedPublishers")
    @Path("/publishers")
    @ApiOperation(value = "Returns a list of Json representations of publishers that are linked " +
            "to the project.  Accepts a UUID of a project.")
    public Response getLinkedPublishers(@Context SecurityContext sc,
                                        @ApiParam(value = "UUID of project") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Publishers(s)",
                "Project UUID", uuid);

        return new ProjectLogic().getLinkedOrganisations(uuid, MapType.PUBLISHER.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getLinkedSubscribers")
    @Path("/subscribers")
    @ApiOperation(value = "Returns a list of Json representations of subscribers that are linked " +
            "to the project.  Accepts a UUID of a project.")
    public Response getLinkedSubscribers(@Context SecurityContext sc,
                                         @ApiParam(value = "UUID of project") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Subscribers(s)",
                "Project UUID", uuid);

        return new ProjectLogic().getLinkedOrganisations(uuid, MapType.SUBSCRIBER.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getLinkedDsasForProject")
    @Path("/dsas")
    @ApiOperation(value = "Returns a list of Json representations of Data Sharing Agreements that are linked " +
            "to the project.  Accepts a UUID of a project.")
    public Response getLinkedDsasForProject(@Context SecurityContext sc,
                                             @ApiParam(value = "UUID of project") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DSA(s)",
                "Project UUID", uuid);

        return new ProjectLogic().getLinkedDsas(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getLinkedScheduleForProject")
    @Path("/schedule")
    @ApiOperation(value = "Returns the schedule that is linked to the project. Accepts a UUID of a project.")
    public Response getLinkedScheduleForProject(@Context SecurityContext sc,
                                            @ApiParam(value = "UUID of project") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Schedule",
                "Project UUID", uuid);

        return new ProjectLogic().getLinkedSchedule(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getLinkedExtractTechnicalDetailsForProject")
    @Path("/extractTechnicalDetails")
    @ApiOperation(value = "Returns the extract technical details linked to the project. Accepts a UUID of a project.")
    public Response getLinkedExtractTechnicalDetailsForProject(@Context SecurityContext sc,
                                                @ApiParam(value = "UUID of project") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Schedule",
                "Project UUID", uuid);

        return new ProjectLogic().getLinkedExtractTechnicalDetails(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getBasePopulations")
    @Path("/basePopulations")
    @ApiOperation(value = "Returns a list of Json representations of cohorts that are linked " +
            "to the project.  Accepts a UUID of a project.")
    public Response getBasePopulations(@Context SecurityContext sc,
                                           @ApiParam(value = "UUID of project") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Cohort(s)",
                "Project UUID", uuid);

        return new ProjectLogic().getBasePopulations(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getDataSets")
    @Path("/dataSets")
    @ApiOperation(value = "Returns a list of Json representations of data sets that are linked " +
            "to the project.  Accepts a UUID of a project.")
    public Response getDataSets(@Context SecurityContext sc,
                                       @ApiParam(value = "UUID of project") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Data set(s)",
                "Project UUID", uuid);

        return new ProjectLogic().getDataSets(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="UserManager.ProjectEndpoint.getProjectApplicationPolicy")
    @Path("/projectApplicationPolicy")
    @ApiOperation(value = "Returns the application policy associated with the project")
    public Response getProjectApplicationPolicy(@Context SecurityContext sc,
                                             @ApiParam(value = "Project id to get the application policy for") @QueryParam("projectUuid") String projectUuid) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "User application policy", "User Id", projectUuid);

        LOG.trace("getUser");

        ProjectApplicationPolicyEntity projectPolicy = new SecurityProjectApplicationPolicyDAL().getProjectApplicationPolicyId(projectUuid);
        if (projectPolicy == null) {
            projectPolicy = new ProjectApplicationPolicyEntity();
        }

        AbstractEndpoint.clearLogbackMarkers();
        return Response
                .ok()
                .entity(projectPolicy)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="UserManager.ProjectEndpoint.setProjectApplicationPolicy")
    @Path("/setProjectApplicationPolicy")
    @RequiresAdmin
    @ApiOperation(value = "Saves application policy associated with a project")
    public Response setUserApplicationPolicy(@Context SecurityContext sc, JsonProjectApplicationPolicy projectApplicationPolicy) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Project application policy", "Project application policy", projectApplicationPolicy);

        LOG.trace("getUser");

        new ProjectApplicationPolicyDAL().saveProjectApplicationPolicyId(projectApplicationPolicy);

        AbstractEndpoint.clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="UserManager.ProjectEndpoint.getApplicationPolicies")
    @Path("/getApplicationPolicies")
    @ApiOperation(value = "Returns a list of application policies")
    public Response getApplicationPolicies(@Context SecurityContext sc) throws Exception {

        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "application policy(s)");

        List<ApplicationPolicyEntity> applicationPolicies = ApplicationPolicyCache.getAllApplicationPolicies();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(applicationPolicies)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getUsers")
    @Path("/getUsers")
    @ApiOperation(value = "Returns a list of available users to assign to the project lead or technical lead")
    public Response getUsers(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Users");

        return new ProjectLogic().getUsers();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ProjectEndpoint.getUsersAssignedToProject")
    @Path("/getUsersAssignedToProject")
    @ApiOperation(value = "Returns a list of Json representations of subscribers that are linked " +
            "to the project.  Accepts a UUID of a project.")
    public Response getUsersAssignedToProject(@Context SecurityContext sc,
                                              @ApiParam(value = "Project id to get the application policy for") @QueryParam("projectUuid") String projectUuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Users assigned to project");

        List<JsonAuthorityToShare> authorities = new org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectDAL().getUsersAssignedToProject(projectUuid);

        return Response
                .ok()
                .entity(authorities)
                .build();
    }
}
