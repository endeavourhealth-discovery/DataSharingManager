package org.endeavourhealth.datasharingmanager.api.Logic;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectScheduleDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonExtractTechnicalDetails;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonProject;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonProjectSchedule;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.ProjectCache;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.UserCache;
import org.endeavourhealth.common.security.usermanagermodel.models.database.UserRegionEntity;
import org.endeavourhealth.common.security.usermanagermodel.models.json.JsonUser;
import org.endeavourhealth.datasharingmanager.api.DAL.*;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

public class ProjectLogic {

    public Response getProjects(String uuid, String searchData, String userId) throws Exception {

        if (uuid == null && searchData == null && userId == null) {
            return getProjectList();
        } else if (userId != null) {
            return getProjectListFilterOnRegion(userId);
        }  else if (uuid != null){
            return getSingleProject(uuid);
        } else {
            return search(searchData);
        }
    }

    private Response getProjectListFilterOnRegion(String userId) throws Exception {

        UserRegionEntity userRegion = UserCache.getUserRegion(userId);

        List<ProjectEntity> dpas = ProjectCache.getAllProjectsForAllChildRegions(userRegion.getRegionId());

        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    public Response postProject(JsonProject project, String userProjectId) throws Exception {

        if (project.getUuid() != null) {
            new ProjectDAL().updateProject(project, userProjectId, false);
        } else {
            project.setUuid(UUID.randomUUID().toString());
            new ProjectDAL().saveProject(project, userProjectId);
        }

        return Response
                .ok()
                .entity(project.getUuid())
                .build();
    }

    public Response updateMappings(JsonProject project, String userProjectId) throws Exception {

        new ProjectDAL().updateProject(project, userProjectId, true);
        return Response
                .ok()
                .entity(project.getUuid())
                .build();
    }

    public Response getUsers() throws Exception {

        List<JsonUser> users = UserCache.getAllUsers();

        return Response
                .ok()
                .entity(users)
                .build();
    }

    public Response getProjectList() throws Exception {

        List<ProjectEntity> dataFlows = new ProjectDAL().getAllProjects();

        return Response
                .ok()
                .entity(dataFlows)
                .build();
    }

    private Response getSingleProject(String uuid) throws Exception {
        ProjectEntity dataFlow = ProjectCache.getProjectDetails(uuid);

        return Response
                .ok()
                .entity(dataFlow)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<ProjectEntity> projects = new ProjectDAL().search(searchData);

        return Response
                .ok()
                .entity(projects)
                .build();
    }

    public Response getLinkedDsas(String projectId) throws Exception {

        List<DataSharingAgreementEntity> ret = new SecurityProjectDAL().getLinkedDsas(projectId);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedSchedule(String projectId) throws Exception {

        ProjectScheduleEntity scheduleEntity =
                new SecurityProjectDAL().getLinkedSchedule(projectId, MapType.SCHEDULE.getMapType());

        JsonProjectSchedule schedule = null;
        if (scheduleEntity != null) {
            schedule = SecurityProjectDAL.setJsonProjectSchedule(scheduleEntity);
        }
        return Response
                .ok()
                .entity(schedule)
                .build();
    }

    public Response getLinkedExtractTechnicalDetails(String projectId) throws Exception {

        ExtractTechnicalDetailsEntity detailsEntity =
                new SecurityProjectDAL().getLinkedExtractTechnicalDetails(projectId, MapType.EXTRACTTECHNICALDETAILS.getMapType());

        JsonExtractTechnicalDetails details = null;
        if (detailsEntity != null) {
            details = SecurityProjectDAL.setJsonExtractTechnicalDetails(detailsEntity);
        }
        return Response
                .ok()
                .entity(details)
                .build();
    }

    public Response getBasePopulations(String projectId) throws Exception {

        List<CohortEntity> ret = new SecurityProjectDAL().getBasePopulations(projectId);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getDataSets(String projectId) throws Exception {

        List<DatasetEntity> ret = new SecurityProjectDAL().getDataSets(projectId);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedOrganisations(String projectId, Short mapType) throws Exception {

        List<OrganisationEntity> ret = new SecurityProjectDAL().getLinkedOrganisations(projectId, mapType);

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}
