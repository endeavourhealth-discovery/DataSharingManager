package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataSharingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDSA;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.DataSharingAgreementCache;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.util.List;

public class DataSharingAgreementDAL {

    private void clearDSACache(String dsaId) throws Exception {
        DataSharingAgreementCache.clearDataSharingAgreementCache(dsaId);
    }

    public List<DataSharingAgreementEntity> getAllDSAs() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DataSharingAgreementEntity> cq = cb.createQuery(DataSharingAgreementEntity.class);
            Root<DataSharingAgreementEntity> rootEntry = cq.from(DataSharingAgreementEntity.class);
            CriteriaQuery<DataSharingAgreementEntity> all = cq.select(rootEntry);
            TypedQuery<DataSharingAgreementEntity> allQuery = entityManager.createQuery(all);
            List<DataSharingAgreementEntity> ret = allQuery.getResultList();

            return ret;

        } finally {
            entityManager.close();
        }

    }

    public void updateDSA(JsonDSA dsa) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            DataSharingAgreementEntity dsaEntity = entityManager.find(DataSharingAgreementEntity.class, dsa.getUuid());

            entityManager.getTransaction().begin();
            dsaEntity.setName(dsa.getName());
            dsaEntity.setDescription(dsa.getDescription());
            dsaEntity.setDerivation(dsa.getDerivation());
            dsaEntity.setDsaStatusId(dsa.getDsaStatusId());
            dsaEntity.setConsentModelId(dsa.getConsentModelId());
            if (dsa.getStartDate() != null) {
                dsaEntity.setStartDate(Date.valueOf(dsa.getStartDate()));
            }
            if (dsa.getEndDate() != null) {
                dsaEntity.setEndDate(Date.valueOf(dsa.getEndDate()));
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDSACache(dsa.getUuid());
    }

    public void saveDSA(JsonDSA dsa) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            DataSharingAgreementEntity dsaEntity = new DataSharingAgreementEntity();
            entityManager.getTransaction().begin();
            dsaEntity.setName(dsa.getName());
            dsaEntity.setDescription(dsa.getDescription());
            dsaEntity.setDerivation(dsa.getDerivation());
            dsaEntity.setDsaStatusId(dsa.getDsaStatusId());
            dsaEntity.setConsentModelId(dsa.getConsentModelId());
            if (dsa.getStartDate() != null) {
                dsaEntity.setStartDate(Date.valueOf(dsa.getStartDate()));
            }
            if (dsa.getEndDate() != null) {
                dsaEntity.setEndDate(Date.valueOf(dsa.getEndDate()));
            }
            dsaEntity.setUuid(dsa.getUuid());
            entityManager.persist(dsaEntity);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDSACache(dsa.getUuid());
    }

    public void deleteDSA(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            DataSharingAgreementEntity dsaEntity = entityManager.find(DataSharingAgreementEntity.class, uuid);
            entityManager.getTransaction().begin();
            entityManager.remove(dsaEntity);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDSACache(uuid);
    }

    public List<DataSharingAgreementEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DataSharingAgreementEntity> cq = cb.createQuery(DataSharingAgreementEntity.class);
            Root<DataSharingAgreementEntity> rootEntry = cq.from(DataSharingAgreementEntity.class);

            Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                    cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

            cq.where(predicate);
            TypedQuery<DataSharingAgreementEntity> query = entityManager.createQuery(cq);
            List<DataSharingAgreementEntity> ret = query.getResultList();

            return ret;

        } finally {
            entityManager.close();
        }

    }

    public List<String> checkDataSharingAgreementsForOrganisation(String odsCode) throws Exception {

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            Query query = entityManager.createQuery(
                    "select de.endpoint from DataSharingAgreementEntity dsa " +
                            "inner join MasterMappingEntity mm on mm.parentUuid = dsa.uuid and mm.parentMapTypeId = :dpaType " +
                            "inner join OrganisationEntity o on o.uuid = mm.childUuid and mm.childMapTypeId = :subscriberType " +
                            "inner join MasterMappingEntity mdf on mdf.parentUuid = dsa.uuid and mdf.parentMapTypeId = :dpaType " +
                            "inner join DataFlowEntity df on df.uuid = mdf.childUuid and mm.childMapTypeId = :dataFlowType " +
                            "inner join MasterMappingEntity mde on mde.parentUuid = df.uuid and mde.parentMapTypeId = :dataFlowType " +
                            "inner join DataExchangeEntity de on de.uuid = mde.childUuid and mde.childMapTypeId = :exchangeType " +
                            "where o.odsCode = :ods " +
                            "and (dsa.startDate is not null and dsa.startDate <= current_date) " +
                            "and (dsa.endDate is null or dsa.endDate >= current_date) " +
                            "and dsa.dsaStatusId = 0 ");
            query.setParameter("dpaType", MapType.DATAPROCESSINGAGREEMENT.getMapType());
            query.setParameter("ods", odsCode);
            query.setParameter("subscriberType", MapType.SUBSCRIBER.getMapType());
            query.setParameter("dataFlowType", MapType.DATAFLOW.getMapType());
            query.setParameter("exchangeType", MapType.DATAEXCHANGE.getMapType());

            List<String> result = query.getResultList();

            return result;

        } finally {
            entityManager.close();
        }

    }
}
