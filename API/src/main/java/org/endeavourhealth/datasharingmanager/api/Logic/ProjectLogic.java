package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.ProjectDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDocumentation;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonExtractTechnicalDetails;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonProject;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonProjectSchedule;
import org.endeavourhealth.core.database.dal.usermanager.caching.ProjectCache;
import org.endeavourhealth.core.database.dal.usermanager.caching.UserCache;
import org.endeavourhealth.core.database.dal.usermanager.models.JsonUser;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.*;
import org.endeavourhealth.core.database.rdbms.usermanager.models.UserRegionEntity;
import org.endeavourhealth.datasharingmanager.api.DAL.*;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

public class ProjectLogic {
    private static ProjectDalI projectRepository = DalProvider.factoryDSMProjectDal();

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

        List<DataSharingAgreementEntity> ret = projectRepository.getLinkedDsas(projectId);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedSchedule(String projectId) throws Exception {

        ProjectScheduleEntity scheduleEntity =
                projectRepository.getLinkedSchedule(projectId, MapType.SCHEDULE.getMapType());

        JsonProjectSchedule schedule = null;
        if (scheduleEntity != null) {
            schedule = projectRepository.setJsonProjectSchedule(scheduleEntity);
        }
        return Response
                .ok()
                .entity(schedule)
                .build();
    }

    public Response getLinkedExtractTechnicalDetails(String projectId) throws Exception {

        ExtractTechnicalDetailsEntity detailsEntity =
                projectRepository.getLinkedExtractTechnicalDetails(projectId, MapType.EXTRACTTECHNICALDETAILS.getMapType());

        JsonExtractTechnicalDetails details = null;
        if (detailsEntity != null) {
            details = projectRepository.setJsonExtractTechnicalDetails(detailsEntity);
        }
        return Response
                .ok()
                .entity(details)
                .build();
    }

    public Response getBasePopulations(String projectId) throws Exception {

        List<CohortEntity> ret = projectRepository.getBasePopulations(projectId);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getDataSets(String projectId) throws Exception {

        List<DataSetEntity> ret = projectRepository.getDataSets(projectId);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getLinkedOrganisations(String projectId, Short mapType) throws Exception {

        List<OrganisationEntity> ret = projectRepository.getLinkedOrganisations(projectId, mapType);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response addDocument(String uuid, JsonDocumentation document, String userProjectID) throws Exception {

        new ProjectDAL().addDocument(uuid, document, userProjectID);

        return Response
                .ok()
                .entity(uuid)
                .build();
    }
}
