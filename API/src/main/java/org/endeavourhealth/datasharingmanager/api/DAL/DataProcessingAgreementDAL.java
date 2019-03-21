package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataProcessingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDPA;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.util.List;

public class DataProcessingAgreementDAL {

    public List<DataProcessingAgreementEntity> getAllDPAs() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataProcessingAgreementEntity> cq = cb.createQuery(DataProcessingAgreementEntity.class);
        Root<DataProcessingAgreementEntity> rootEntry = cq.from(DataProcessingAgreementEntity.class);
        CriteriaQuery<DataProcessingAgreementEntity> all = cq.select(rootEntry);
        TypedQuery<DataProcessingAgreementEntity> allQuery = entityManager.createQuery(all);
        List<DataProcessingAgreementEntity> ret =  allQuery.getResultList();

        entityManager.close();

        return ret;
    }

    public DataProcessingAgreementEntity getDPA(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataProcessingAgreementEntity ret = entityManager.find(DataProcessingAgreementEntity.class, uuid);

        entityManager.close();

        return ret;
    }

    public void updateDPA(JsonDPA dpa) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

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

        entityManager.close();
    }

    public void saveDPA(JsonDPA dpa) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

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

        entityManager.close();
    }

    public void deleteDPA(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataProcessingAgreementEntity dpaEntity = entityManager.find(DataProcessingAgreementEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(dpaEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public List<DataProcessingAgreementEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataProcessingAgreementEntity> cq = cb.createQuery(DataProcessingAgreementEntity.class);
        Root<DataProcessingAgreementEntity> rootEntry = cq.from(DataProcessingAgreementEntity.class);

        Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

        cq.where(predicate);
        TypedQuery<DataProcessingAgreementEntity> query = entityManager.createQuery(cq);
        List<DataProcessingAgreementEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public List<DataProcessingAgreementEntity> getDPAsFromList(List<String> dpas) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataProcessingAgreementEntity> cq = cb.createQuery(DataProcessingAgreementEntity.class);
        Root<DataProcessingAgreementEntity> rootEntry = cq.from(DataProcessingAgreementEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(dpas);

        cq.where(predicate);
        TypedQuery<DataProcessingAgreementEntity> query = entityManager.createQuery(cq);

        List<DataProcessingAgreementEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public List<DataProcessingAgreementEntity> getDataProcessingAgreementsForOrganisation(String odsCode) throws Exception {

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        Query query = entityManager.createQuery(
                "select dpa from DataProcessingAgreementEntity dpa " +
                        "inner join MasterMappingEntity mm on mm.parentUuid = dpa.uuid and mm.parentMapTypeId = :dpaType " +
                        "inner join OrganisationEntity o on o.uuid = mm.childUuid " +
                        "where o.odsCode = :ods " +
                        "and mm.childMapTypeId = :publisherType " +
                        "and (dpa.startDate is not null and dpa.startDate <= current_date) " +
                        "and (dpa.endDate is null or dpa.endDate >= current_date) " +
                        "and dpa.dsaStatusId = 0 ");
        query.setParameter("dpaType", MapType.DATAPROCESSINGAGREEMENT.getMapType());
        query.setParameter("ods", odsCode);
        query.setParameter("publisherType", MapType.PUBLISHER.getMapType());

        List<DataProcessingAgreementEntity> result = query.getResultList();

        entityManager.close();

        return result;
    }

    public List<DataProcessingAgreementEntity> getDataProcessingAgreementsForOrganisationAndSystemType(String odsCode, String systemName) throws Exception {

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        Query query = entityManager.createQuery(
                "select dpa from DataProcessingAgreementEntity dpa " +
                        "inner join MasterMappingEntity mm on mm.parentUuid = dpa.uuid and mm.parentMapTypeId = :dpaType " +
                        "inner join OrganisationEntity o on o.uuid = mm.childUuid and mm.childMapTypeId = :publisherType " +
                        "inner join MasterMappingEntity mdf on mdf.parentUuid = dpa.uuid and mdf.parentMapTypeId = :dpaType " +
                        "inner join DataFlowEntity df on df.uuid = mdf.childUuid and mm.childMapTypeId = :dataFlowType " +
                        "inner join MasterMappingEntity mde on mde.parentUuid = df.uuid and mde.parentMapTypeId = :dataFlowType " +
                        "inner join DataExchangeEntity de on de.uuid = mde.childUuid and mde.childMapTypeId = :exchangeType " +
                        "where o.odsCode = :ods " +
                        " and de.systemName = :sysName " +
                        "and (dpa.startDate is not null and dpa.startDate <= current_date) " +
                        "and (dpa.endDate is null or dpa.endDate >= current_date) " +
                        "and dpa.dsaStatusId = 0 ");
        query.setParameter("dpaType", MapType.DATAPROCESSINGAGREEMENT.getMapType());
        query.setParameter("ods", odsCode);
        query.setParameter("sysName", systemName);
        query.setParameter("publisherType", MapType.PUBLISHER.getMapType());
        query.setParameter("dataFlowType", MapType.DATAFLOW.getMapType());
        query.setParameter("exchangeType", MapType.DATAEXCHANGE.getMapType());

        List<DataProcessingAgreementEntity> result = query.getResultList();

        entityManager.close();

        return result;
    }
}
