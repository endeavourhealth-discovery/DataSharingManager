package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.CohortEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonCohort;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
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

    public CohortEntity getCohort(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            CohortEntity ret = entityManager.find(CohortEntity.class, uuid);

            return ret;
        } finally {
            entityManager.close();
        }
    }

    public void updateCohort(JsonCohort cohort, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CohortEntity newCohort = new CohortEntity(cohort);

        CohortEntity oldCohortEntity = entityManager.find(CohortEntity.class, cohort.getUuid());

        String auditJson = new AuditCompareLogic().getAuditJson("Cohort Edited", oldCohortEntity, newCohort);

        try {

            entityManager.getTransaction().begin();
            oldCohortEntity.setName(cohort.getName());
            oldCohortEntity.setConsentModelId(cohort.getConsentModelId());
            oldCohortEntity.setTechnicalDefinition(cohort.getTechnicalDefinition());
            oldCohortEntity.setDescription(cohort.getDescription());

            new MasterMappingDAL().updateCohortMappings(cohort, userProjectId);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.EDIT, ItemType.COHORT, null, null, auditJson);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void saveCohort(JsonCohort cohort, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CohortEntity cohortEntity = new CohortEntity();
            entityManager.getTransaction().begin();
            cohortEntity.setName(cohort.getName());
            cohortEntity.setConsentModelId(cohort.getConsentModelId());
            cohortEntity.setDescription(cohort.getDescription());
            cohortEntity.setTechnicalDefinition(cohort.getTechnicalDefinition());
            cohortEntity.setUuid(cohort.getUuid());

            String auditJson = new AuditCompareLogic().getAuditJson("Cohort Created", null, cohortEntity);

            entityManager.persist(cohortEntity);

            new MasterMappingDAL().updateCohortMappings(cohort, userProjectId);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.ADD, ItemType.COHORT, null, null, auditJson);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void deleteCohort(String uuid, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CohortEntity oldCohortEntity = entityManager.find(CohortEntity.class, uuid);
            String auditJson = new AuditCompareLogic().getAuditJson("Cohort Deleted", oldCohortEntity, null);

            CohortEntity cohortEntity = entityManager.find(CohortEntity.class, uuid);
            entityManager.getTransaction().begin();
            entityManager.remove(cohortEntity);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.DELETE, ItemType.COHORT, null, null, auditJson);

            entityManager.getTransaction().commit();
            // Any reason for lack of catch/rollback?
        } finally {
            entityManager.close();
        }
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
