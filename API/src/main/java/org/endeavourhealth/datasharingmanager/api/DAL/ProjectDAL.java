package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDocumentation;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonProject;
import org.endeavourhealth.core.database.dal.usermanager.caching.ProjectCache;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DocumentationEntity;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ProjectEntity;
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

    public void updateProject(JsonProject project, String userProjectId, boolean withMappings) throws Exception {
        ProjectEntity oldProjectEntity = _entityManager.find(ProjectEntity.class, project.getUuid());
        oldProjectEntity.setMappingsFromDAL();

        try {
            _entityManager.getTransaction().begin();

            ProjectEntity newProject = new ProjectEntity(project);
            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Project edited", oldProjectEntity, newProject);

            if (withMappings) {
                _masterMappingDAL.updateProjectMappings(project, oldProjectEntity, auditJson);
            }

            oldProjectEntity.updateFromJson(project);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.EDIT, ItemType.PROJECT, auditJson);

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

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.ADD, ItemType.PROJECT, auditJson);

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
            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.DELETE, ItemType.PROJECT, auditJson);

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

    public void addDocument(String uuid, JsonDocumentation document, String userProjectId) throws Exception {
        ProjectEntity oldProjectEntity = ProjectCache.getProjectDetails(uuid);
        oldProjectEntity.setMappingsFromDAL();

        try {
            _entityManager.getTransaction().begin();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Project edited", oldProjectEntity, oldProjectEntity);

            List<JsonDocumentation> documents = new ArrayList();
            JsonDocumentation docJSon = null;
            DocumentationEntity docEntity = null;
            for (String doc : oldProjectEntity.getDocumentations()) {
                if (doc != null) {
                    docEntity = new DocumentationDAL(_entityManager).getDocument(doc);
                    docJSon = new JsonDocumentation();
                    docJSon.setFilename(docEntity.getFilename());
                    docJSon.setFileData(docEntity.getFileData());
                    docJSon.setTitle(docEntity.getTitle());
                    docJSon.setUuid(docEntity.getUuid());
                    documents.add(docJSon);
                }
            }

            JsonProject project = new JsonProject();
            documents.add(document);
            project.setUuid(uuid);
            project.setDocumentations(documents);

            _masterMappingDAL.updateDocumentsAndAddToAudit(uuid, (project == null ? null : oldProjectEntity.getDocumentations()),
                    (oldProjectEntity == null ? null : project.getDocumentations()), MapType.PROJECT.getMapType(), auditJson);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.EDIT, ItemType.PROJECT, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearProjectCache(uuid);
    }
}
