package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.CohortEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonCohort;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.CohortCache;
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
import java.util.List;

public class CohortDAL {

    private void clearCohortCache(String cohortId) throws Exception {
        CohortCache.clearCohortCache(cohortId);
    }

    public List<CohortEntity> getAllCohorts() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<CohortEntity> cq = cb.createQuery(CohortEntity.class);
            Root<CohortEntity> rootEntry = cq.from(CohortEntity.class);
            CriteriaQuery<CohortEntity> all = cq.select(rootEntry);
            TypedQuery<CohortEntity> allQuery = entityManager.createQuery(all);
            List<CohortEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }
    }

    public void updateCohort(JsonCohort cohort, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CohortEntity oldCohortEntity = entityManager.find(CohortEntity.class, cohort.getUuid());
        oldCohortEntity.setDpas(new SecurityMasterMappingDAL().getParentMappings(cohort.getUuid(), MapType.COHORT.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType()));
        CohortEntity newCohort = new CohortEntity(cohort);
        JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Cohort edited", oldCohortEntity, newCohort);

        try {
            entityManager.getTransaction().begin();
            oldCohortEntity.setName(cohort.getName());
            oldCohortEntity.setConsentModelId(cohort.getConsentModelId());
            oldCohortEntity.setDescription(cohort.getDescription());
            oldCohortEntity.setTechnicalDefinition(cohort.getTechnicalDefinition());

            auditJson = new MasterMappingDAL(entityManager).updateCohortMappings(cohort, oldCohortEntity, auditJson);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.EDIT, ItemType.COHORT, null, null, auditJson);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearCohortCache(cohort.getUuid());
    }

    public void saveCohort(JsonCohort cohort, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        CohortEntity cohortEntity = new CohortEntity();

        try {
            entityManager.getTransaction().begin();
            cohortEntity.setName(cohort.getName());
            cohortEntity.setConsentModelId(cohort.getConsentModelId());
            cohortEntity.setDescription(cohort.getDescription());
            cohortEntity.setTechnicalDefinition(cohort.getTechnicalDefinition());
            cohortEntity.setUuid(cohort.getUuid());

            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Cohort created", null, cohortEntity);

            auditJson = new MasterMappingDAL(entityManager).updateCohortMappings(cohort, null, auditJson);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.ADD, ItemType.COHORT, null, null, auditJson);

            entityManager.persist(cohortEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearCohortCache(cohort.getUuid());
    }

    public void deleteCohort(String uuid, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            entityManager.getTransaction().begin();

            CohortEntity oldCohortEntity = entityManager.find(CohortEntity.class, uuid);
            oldCohortEntity.setDpas(new SecurityMasterMappingDAL().getParentMappings(uuid, MapType.COHORT.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType()));
            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Cohort deleted", oldCohortEntity, null);
            auditJson = new MasterMappingDAL(entityManager).updateCohortMappings(null, oldCohortEntity, auditJson);
            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.DELETE, ItemType.COHORT, null, null, auditJson);

            entityManager.remove(oldCohortEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearCohortCache(uuid);
    }

    public List<CohortEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<CohortEntity> cq = cb.createQuery(CohortEntity.class);
            Root<CohortEntity> rootEntry = cq.from(CohortEntity.class);

            Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                    cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

            cq.where(predicate);
            TypedQuery<CohortEntity> query = entityManager.createQuery(cq);
            List<CohortEntity> ret = query.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }

    }
}
