package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonProject;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.ProjectCache;
import org.endeavourhealth.datasharingmanager.api.Logic.ExtractTechnicalDetailsLogic;
import org.endeavourhealth.uiaudit.dal.UIAuditJDBCDAL;
import org.endeavourhealth.uiaudit.enums.AuditAction;
import org.endeavourhealth.uiaudit.enums.ItemType;
import org.endeavourhealth.uiaudit.logic.AuditCompareLogic;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.util.*;

public class ProjectDAL {

    private EntityManager _entityManager;
    private MasterMappingDAL _masterMappingDAL;
    private AuditCompareLogic _auditCompareLogic;
    private UIAuditJDBCDAL _uiAuditJDBCDAL;

    public ProjectDAL() throws Exception {
        _entityManager = ConnectionManager.getDsmEntityManager();
        _masterMappingDAL = new MasterMappingDAL(_entityManager);
        _auditCompareLogic = new AuditCompareLogic();
        _uiAuditJDBCDAL = new UIAuditJDBCDAL();
    }

    private void clearProjectCache(String projectId) throws Exception {
        ProjectCache.clearProjectCache(projectId);
    }

    public List<ProjectEntity> getAllProjects() throws Exception {
         try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);
            Root<ProjectEntity> rootEntry = cq.from(ProjectEntity.class);
            CriteriaQuery<ProjectEntity> all = cq.select(rootEntry);
            TypedQuery<ProjectEntity> allQuery = _entityManager.createQuery(all);
            List<ProjectEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
             _entityManager.close();
        }

    }

    public void updateProject(JsonProject project, String userProjectId) throws Exception {
        ProjectEntity oldProjectEntity = _entityManager.find(ProjectEntity.class, project.getUuid());
        oldProjectEntity.setMappingsFromDAL();

        try {
            _entityManager.getTransaction().begin();

            ProjectEntity newProject = new ProjectEntity(project);
            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Project edited", oldProjectEntity, newProject);

            _masterMappingDAL.updateProjectMappings(project, oldProjectEntity, auditJson);

            oldProjectEntity.updateFromJson(project);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId,
                    AuditAction.EDIT, ItemType.PROJECT, null, null, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearProjectCache(project.getUuid());
    }

    public void saveProject(JsonProject project, String userProjectId) throws Exception {
        ProjectEntity projectEntity = new ProjectEntity(project);

        try {
            _entityManager.getTransaction().begin();
            _entityManager.persist(projectEntity);

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Project created", null, projectEntity);

            _masterMappingDAL.updateProjectMappings(project, null, auditJson);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId,
                    AuditAction.ADD, ItemType.PROJECT, null, null, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearProjectCache(project.getUuid());
    }

    public void deleteProject(String uuid, String userProjectId) throws Exception {
        try {
            _entityManager.getTransaction().begin();

            ProjectEntity oldProjectEntity = _entityManager.find(ProjectEntity.class, uuid);
            oldProjectEntity.setMappingsFromDAL();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Project deleted", oldProjectEntity, null);
            _masterMappingDAL.updateProjectMappings(null, oldProjectEntity, auditJson);
            _uiAuditJDBCDAL.addToAuditTrail(userProjectId,
                    AuditAction.DELETE, ItemType.PROJECT, null, null, auditJson);

            _entityManager.remove(oldProjectEntity);
            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearProjectCache(uuid);
    }

    public List<ProjectEntity> search(String expression) throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);
            Root<ProjectEntity> rootEntry = cq.from(ProjectEntity.class);

            Predicate predicate = cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%");

            cq.where(predicate);
            TypedQuery<ProjectEntity> query = _entityManager.createQuery(cq);
            List<ProjectEntity> ret = query.getResultList();

            return ret;

        } finally {
            _entityManager.close();
        }
    }

    public boolean checkProjectIsActive(String projectId) throws Exception {
        boolean projectActive = false;

        ProjectEntity project = ProjectCache.getProjectDetails(projectId);

        if (project != null) {
            if (project.getProjectStatusId() != null && project.getProjectStatusId() == 0) {
                projectActive = true;
            }
        }

        return projectActive;
    }
}
