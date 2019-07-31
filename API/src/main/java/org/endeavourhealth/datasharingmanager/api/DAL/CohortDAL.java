package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.CohortEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonCohort;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

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

    public void updateCohort(JsonCohort cohort) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            CohortEntity cohortEntity = entityManager.find(CohortEntity.class, cohort.getUuid());
            entityManager.getTransaction().begin();
            cohortEntity.setName(cohort.getName());
            cohortEntity.setConsentModelId(cohort.getConsentModelId());
            cohortEntity.setDescription(cohort.getDescription());
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void saveCohort(JsonCohort cohort) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CohortEntity cohortEntity = new CohortEntity();
            entityManager.getTransaction().begin();
            cohortEntity.setName(cohort.getName());
            cohortEntity.setConsentModelId(cohort.getConsentModelId());
            cohortEntity.setDescription(cohort.getDescription());
            cohortEntity.setUuid(cohort.getUuid());
            entityManager.persist(cohortEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void deleteCohort(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CohortEntity cohortEntity = entityManager.find(CohortEntity.class, uuid);
            entityManager.getTransaction().begin();
            entityManager.remove(cohortEntity);
            entityManager.getTransaction().commit();
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
