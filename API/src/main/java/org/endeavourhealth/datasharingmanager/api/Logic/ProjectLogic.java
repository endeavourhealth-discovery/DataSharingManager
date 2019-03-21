package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonProject;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.ProjectCache;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.UserCache;
import org.endeavourhealth.common.security.usermanagermodel.models.json.JsonUser;
import org.endeavourhealth.datasharingmanager.api.DAL.MasterMappingDAL;
import org.endeavourhealth.datasharingmanager.api.DAL.ProjectDAL;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

public class ProjectLogic {

    public Response getProjects(String uuid, String searchData) throws Exception {

        if (uuid == null && searchData == null) {

            return getProjectList();
        } else if (uuid != null){
            return getSingleProject(uuid);
        } else {
            return search(searchData);
        }
    }

    public Response postProject(JsonProject project) throws Exception {

        if (project.getUuid() != null) {
            new MasterMappingDAL().deleteAllMappings(project.getUuid());
            new ProjectDAL().updateProject(project);
        } else {
            project.setUuid(UUID.randomUUID().toString());
            new ProjectDAL().saveProject(project);
        }

        new MasterMappingDAL().saveProjectMappings(project);

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
