package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDataSharingSummary;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DataSharingSummaryEntity;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class DataSharingSummaryDAL {

    public List<DataSharingSummaryEntity> getAllDataSharingSummaries() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DataSharingSummaryEntity> cq = cb.createQuery(DataSharingSummaryEntity.class);
            Root<DataSharingSummaryEntity> rootEntry = cq.from(DataSharingSummaryEntity.class);
            CriteriaQuery<DataSharingSummaryEntity> all = cq.select(rootEntry);
            TypedQuery<DataSharingSummaryEntity> allQuery = entityManager.createQuery(all);
            List<DataSharingSummaryEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }

    }

    public DataSharingSummaryEntity getDataSharingSummary(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DataSharingSummaryEntity ret = entityManager.find(DataSharingSummaryEntity.class, uuid);

            return ret;
        } finally {
            entityManager.close();
        }

    }

    public void updateDataSharingSummary(JsonDataSharingSummary dataSharingSummary) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DataSharingSummaryEntity dataSharingSummaryEntity = entityManager.find(DataSharingSummaryEntity.class, dataSharingSummary.getUuid());
            entityManager.getTransaction().begin();
            dataSharingSummaryEntity.setName(dataSharingSummary.getName());
            dataSharingSummaryEntity.setDescription(dataSharingSummary.getDescription());
            dataSharingSummaryEntity.setPurpose(dataSharingSummary.getPurpose());
            dataSharingSummaryEntity.setNatureOfInformationId(dataSharingSummary.getNatureOfInformationId());
            dataSharingSummaryEntity.setSchedule2Condition(dataSharingSummary.getSchedule2Condition());
            dataSharingSummaryEntity.setBenefitToSharing(dataSharingSummary.getBenefitToSharing());
            dataSharingSummaryEntity.setOverviewOfDataItems(dataSharingSummary.getOverviewOfDataItems());
            dataSharingSummaryEntity.setFormatTypeId(dataSharingSummary.getFormatTypeId());
            dataSharingSummaryEntity.setDataSubjectTypeId(dataSharingSummary.getDataSubjectTypeId());
            dataSharingSummaryEntity.setNatureOfPersonsAccessingData(dataSharingSummary.getNatureOfPersonsAccessingData());
            dataSharingSummaryEntity.setReviewCycleId(dataSharingSummary.getReviewCycleId());
            dataSharingSummaryEntity.setReviewDate(dataSharingSummary.getReviewDate());
            dataSharingSummaryEntity.setStartDate(dataSharingSummary.getStartDate());
            dataSharingSummaryEntity.setEvidenceOfAgreement(dataSharingSummary.getEvidenceOfAgreement());
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void saveDataSharingSummary(JsonDataSharingSummary dataSharingSummary) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DataSharingSummaryEntity dataSharingSummaryEntity = new DataSharingSummaryEntity();
            entityManager.getTransaction().begin();
            dataSharingSummaryEntity.setName(dataSharingSummary.getName());
            dataSharingSummaryEntity.setDescription(dataSharingSummary.getDescription());
            dataSharingSummaryEntity.setPurpose(dataSharingSummary.getPurpose());
            dataSharingSummaryEntity.setNatureOfInformationId(dataSharingSummary.getNatureOfInformationId());
            dataSharingSummaryEntity.setSchedule2Condition(dataSharingSummary.getSchedule2Condition());
            dataSharingSummaryEntity.setBenefitToSharing(dataSharingSummary.getBenefitToSharing());
            dataSharingSummaryEntity.setOverviewOfDataItems(dataSharingSummary.getOverviewOfDataItems());
            dataSharingSummaryEntity.setFormatTypeId(dataSharingSummary.getFormatTypeId());
            dataSharingSummaryEntity.setDataSubjectTypeId(dataSharingSummary.getDataSubjectTypeId());
            dataSharingSummaryEntity.setNatureOfPersonsAccessingData(dataSharingSummary.getNatureOfPersonsAccessingData());
            dataSharingSummaryEntity.setReviewCycleId(dataSharingSummary.getReviewCycleId());
            dataSharingSummaryEntity.setReviewDate(dataSharingSummary.getReviewDate());
            dataSharingSummaryEntity.setStartDate(dataSharingSummary.getStartDate());
            dataSharingSummaryEntity.setEvidenceOfAgreement(dataSharingSummary.getEvidenceOfAgreement());
            dataSharingSummaryEntity.setUuid(dataSharingSummary.getUuid());
            entityManager.persist(dataSharingSummaryEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void deleteDataSharingSummary(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DataSharingSummaryEntity dataSharingSummaryEntity = entityManager.find(DataSharingSummaryEntity.class, uuid);
            entityManager.getTransaction().begin();
            entityManager.remove(dataSharingSummaryEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public List<DataSharingSummaryEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DataSharingSummaryEntity> cq = cb.createQuery(DataSharingSummaryEntity.class);
            Root<DataSharingSummaryEntity> rootEntry = cq.from(DataSharingSummaryEntity.class);

            Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                    cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

            cq.where(predicate);
            TypedQuery<DataSharingSummaryEntity> query = entityManager.createQuery(cq);
            List<DataSharingSummaryEntity> ret = query.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }
    }
}
