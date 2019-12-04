package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
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

    private void clearProjectCache(String projectId) throws Exception {
        ProjectCache.clearProjectCache(projectId);
    }

    public List<ProjectEntity> getAllProjects() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);
            Root<ProjectEntity> rootEntry = cq.from(ProjectEntity.class);
            CriteriaQuery<ProjectEntity> all = cq.select(rootEntry);
            TypedQuery<ProjectEntity> allQuery = entityManager.createQuery(all);
            List<ProjectEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }

    }

    public void updateProject(JsonProject project, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        ProjectEntity oldProjectEntity = entityManager.find(ProjectEntity.class, project.getUuid());
        oldProjectEntity.setDsas(new SecurityMasterMappingDAL().getParentMappings(project.getUuid(), MapType.PROJECT.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType()));
        oldProjectEntity.setPublishers(new SecurityMasterMappingDAL().getChildMappings(project.getUuid(), MapType.PROJECT.getMapType(), MapType.PUBLISHER.getMapType()));
        oldProjectEntity.setSubscribers(new SecurityMasterMappingDAL().getChildMappings(project.getUuid(), MapType.PROJECT.getMapType(), MapType.SUBSCRIBER.getMapType()));
        oldProjectEntity.setDocumentations(new SecurityMasterMappingDAL().getChildMappings(project.getUuid(), MapType.PROJECT.getMapType(), MapType.DOCUMENT.getMapType()));
        oldProjectEntity.setCohorts(new SecurityMasterMappingDAL().getChildMappings(project.getUuid(), MapType.PROJECT.getMapType(), MapType.COHORT.getMapType()));
        oldProjectEntity.setDataSets(new SecurityMasterMappingDAL().getChildMappings(project.getUuid(), MapType.PROJECT.getMapType(), MapType.DATASET.getMapType()));
        oldProjectEntity.setSchedules(new SecurityMasterMappingDAL().getChildMappings(project.getUuid(), MapType.PROJECT.getMapType(), MapType.SCHEDULE.getMapType()));
        oldProjectEntity.setExtractTechnicalDetails(new ExtractTechnicalDetailsLogic().getAssociatedExtractTechnicalDetails(project.getUuid(), MapType.PROJECT.getMapType()));

        ProjectEntity newProject = new ProjectEntity(project);

        JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Project edited", oldProjectEntity, newProject);

        try {
            ProjectEntity projectEntity = entityManager.find(ProjectEntity.class, project.getUuid());
            entityManager.getTransaction().begin();
            projectEntity.setName(project.getName());
            projectEntity.setLeadUser(project.getLeadUser());
            projectEntity.setTechnicalLeadUser(project.getTechnicalLeadUser());
            projectEntity.setConsentModelId(project.getConsentModelId());
            projectEntity.setDeidentificationLevel(project.getDeidentificationLevel());
            projectEntity.setProjectTypeId(project.getProjectTypeId());
            projectEntity.setSecurityInfrastructureId(project.getSecurityInfrastructureId());
            projectEntity.setIpAddress(project.getIpAddress());
            projectEntity.setSummary(project.getSummary());
            projectEntity.setBusinessCase(project.getBusinessCase());
            projectEntity.setObjectives(project.getObjectives());
            projectEntity.setSecurityArchitectureId(project.getSecurityArchitectureId());
            projectEntity.setStorageProtocolId(project.getStorageProtocolId());
            projectEntity.setBusinessCaseStatus(project.getBusinessCaseStatus());
            projectEntity.setFlowScheduleId(project.getFlowScheduleId());
            projectEntity.setProjectStatusId(project.getProjectStatusId());
            if (project.getStartDate() != null) {
                projectEntity.setStartDate(Date.valueOf(project.getStartDate()));
            }
            if (project.getEndDate() != null) {
                projectEntity.setEndDate(Date.valueOf(project.getEndDate()));
            }

            auditJson = new MasterMappingDAL().updateProjectMappings(project, oldProjectEntity, auditJson, entityManager);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.EDIT, ItemType.PROJECT, null, null, auditJson);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearProjectCache(project.getUuid());
    }

    public void saveProject(JsonProject project) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            ProjectEntity projectEntity = new ProjectEntity();
            entityManager.getTransaction().begin();
            projectEntity.setUuid(project.getUuid());
            projectEntity.setName(project.getName());
            projectEntity.setLeadUser(project.getLeadUser());
            projectEntity.setTechnicalLeadUser(project.getTechnicalLeadUser());
            projectEntity.setConsentModelId(project.getConsentModelId());
            projectEntity.setDeidentificationLevel(project.getDeidentificationLevel());
            projectEntity.setProjectTypeId(project.getProjectTypeId());
            projectEntity.setSecurityInfrastructureId(project.getSecurityInfrastructureId());
            projectEntity.setIpAddress(project.getIpAddress());
            projectEntity.setSummary(project.getSummary());
            projectEntity.setBusinessCase(project.getBusinessCase());
            projectEntity.setObjectives(project.getObjectives());
            projectEntity.setSecurityArchitectureId(project.getSecurityArchitectureId());
            projectEntity.setStorageProtocolId(project.getStorageProtocolId());
            projectEntity.setBusinessCaseStatus(project.getBusinessCaseStatus());
            projectEntity.setFlowScheduleId(project.getFlowScheduleId());
            projectEntity.setProjectStatusId(project.getProjectStatusId());
            if (project.getStartDate() != null) {
                projectEntity.setStartDate(Date.valueOf(project.getStartDate()));
            }
            if (project.getEndDate() != null) {
                projectEntity.setEndDate(Date.valueOf(project.getEndDate()));
            }
            entityManager.persist(projectEntity);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearProjectCache(project.getUuid());
    }

    public void deleteProject(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            ProjectEntity projectEntity = entityManager.find(ProjectEntity.class, uuid);
            entityManager.getTransaction().begin();
            entityManager.remove(projectEntity);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearProjectCache(uuid);
    }

    public List<ProjectEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);
            Root<ProjectEntity> rootEntry = cq.from(ProjectEntity.class);

            Predicate predicate = cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%");

            cq.where(predicate);
            TypedQuery<ProjectEntity> query = entityManager.createQuery(cq);
            List<ProjectEntity> ret = query.getResultList();

            return ret;

        } finally {
            entityManager.close();
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
