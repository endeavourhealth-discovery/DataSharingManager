package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataProcessingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDPA;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.DataProcessingAgreementCache;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.util.List;

public class DataProcessingAgreementDAL {

    private void clearDPACache(String dpaId) throws Exception {
        DataProcessingAgreementCache.clearDataProcessingAgreementCache(dpaId);
    }

    public List<DataProcessingAgreementEntity> getAllDPAs() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DataProcessingAgreementEntity> cq = cb.createQuery(DataProcessingAgreementEntity.class);
            Root<DataProcessingAgreementEntity> rootEntry = cq.from(DataProcessingAgreementEntity.class);
            CriteriaQuery<DataProcessingAgreementEntity> all = cq.select(rootEntry);
            TypedQuery<DataProcessingAgreementEntity> allQuery = entityManager.createQuery(all);
            List<DataProcessingAgreementEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }

    }

    public void updateDPA(JsonDPA dpa) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DataProcessingAgreementEntity dpaEntity = entityManager.find(DataProcessingAgreementEntity.class, dpa.getUuid());
            entityManager.getTransaction().begin();
            dpaEntity.setName(dpa.getName());
            dpaEntity.setDescription(dpa.getDescription());
            dpaEntity.setDerivation(dpa.getDerivation());
            dpaEntity.setPublisherInformation(dpa.getPublisherInformation());
            dpaEntity.setPublisherContractInformation(dpa.getPublisherContractInformation());
            dpaEntity.setPublisherDataset(dpa.getPublisherDataset());
            dpaEntity.setDsaStatusId(dpa.getDsaStatusId());
            dpaEntity.setReturnToSenderPolicy(dpa.getReturnToSenderPolicy());
            if (dpa.getStartDate() != null) {
                dpaEntity.setStartDate(Date.valueOf(dpa.getStartDate()));
            }
            if (dpa.getEndDate() != null) {
                dpaEntity.setEndDate(Date.valueOf(dpa.getEndDate()));
            }
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDPACache(dpa.getUuid());
    }

    public void saveDPA(JsonDPA dpa) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DataProcessingAgreementEntity dpaEntity = new DataProcessingAgreementEntity();
            entityManager.getTransaction().begin();
            dpaEntity.setName(dpa.getName());
            dpaEntity.setName(dpa.getName());
            dpaEntity.setDescription(dpa.getDescription());
            dpaEntity.setDerivation(dpa.getDerivation());
            dpaEntity.setPublisherInformation(dpa.getPublisherInformation());
            dpaEntity.setPublisherContractInformation(dpa.getPublisherContractInformation());
            dpaEntity.setPublisherDataset(dpa.getPublisherDataset());
            dpaEntity.setDsaStatusId(dpa.getDsaStatusId());
            dpaEntity.setReturnToSenderPolicy(dpa.getReturnToSenderPolicy());
            if (dpa.getStartDate() != null) {
                dpaEntity.setStartDate(Date.valueOf(dpa.getStartDate()));
            }
            if (dpa.getEndDate() != null) {
                dpaEntity.setEndDate(Date.valueOf(dpa.getEndDate()));
            }
            dpaEntity.setUuid(dpa.getUuid());
            entityManager.persist(dpaEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDPACache(dpa.getUuid());
    }

    public void deleteDPA(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DataProcessingAgreementEntity dpaEntity = entityManager.find(DataProcessingAgreementEntity.class, uuid);
            entityManager.getTransaction().begin();
            entityManager.remove(dpaEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDPACache(uuid);
    }

    public List<DataProcessingAgreementEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DataProcessingAgreementEntity> cq = cb.createQuery(DataProcessingAgreementEntity.class);
            Root<DataProcessingAgreementEntity> rootEntry = cq.from(DataProcessingAgreementEntity.class);

            Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                    cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

            cq.where(predicate);
            TypedQuery<DataProcessingAgreementEntity> query = entityManager.createQuery(cq);
            List<DataProcessingAgreementEntity> ret = query.getResultList();

            return ret;

        } finally {
            entityManager.close();
        }
    }
}
