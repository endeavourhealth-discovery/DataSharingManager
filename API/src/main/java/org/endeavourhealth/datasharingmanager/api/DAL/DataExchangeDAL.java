package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataExchangeEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDataExchange;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class DataExchangeDAL {

    public List<DataExchangeEntity> getAllDataExchanges() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataExchangeEntity> cq = cb.createQuery(DataExchangeEntity.class);
        Root<DataExchangeEntity> rootEntry = cq.from(DataExchangeEntity.class);
        CriteriaQuery<DataExchangeEntity> all = cq.select(rootEntry);
        TypedQuery<DataExchangeEntity> allQuery = entityManager.createQuery(all);
        List<DataExchangeEntity> ret =  allQuery.getResultList();

        entityManager.close();

        return ret;
    }

    public DataExchangeEntity getDataExchange(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataExchangeEntity ret = entityManager.find(DataExchangeEntity.class, uuid);

        entityManager.close();

        return ret;
    }

    public void updateDataExchange(JsonDataExchange dataExchange) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataExchangeEntity dataExchangeEntity = entityManager.find(DataExchangeEntity.class, dataExchange.getUuid());
        entityManager.getTransaction().begin();
        dataExchangeEntity.setName(dataExchange.getName());
        dataExchangeEntity.setPublisher(dataExchange.isPublisher() ? (byte)1 : (byte)0);
        dataExchangeEntity.setSystemName(dataExchange.getSystemName());
        dataExchangeEntity.setDirectionId(dataExchange.getDirectionId());
        dataExchangeEntity.setFlowScheduleId(dataExchange.getFlowScheduleId());
        dataExchangeEntity.setApproximateVolume(dataExchange.getApproximateVolume());
        dataExchangeEntity.setDataExchangeMethodId(dataExchange.getDataExchangeMethodId());
        dataExchangeEntity.setSecurityInfrastructureId(dataExchange.getSecurityInfrastructureId());
        dataExchangeEntity.setSecurityArchitectureId(dataExchange.getSecurityArchitectureId());
        dataExchangeEntity.setEndpoint(dataExchange.getEndpoint());
        dataExchangeEntity.setFlowStatusId(dataExchange.getFlowStatusId());

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void saveDataExchange(JsonDataExchange dataExchange) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataExchangeEntity dataExchangeEntity = new DataExchangeEntity();
        entityManager.getTransaction().begin();
        dataExchangeEntity.setName(dataExchange.getName());
        dataExchangeEntity.setPublisher(dataExchange.isPublisher() ? (byte)1 : (byte)0);
        dataExchangeEntity.setSystemName(dataExchange.getSystemName());
        dataExchangeEntity.setDirectionId(dataExchange.getDirectionId());
        dataExchangeEntity.setFlowScheduleId(dataExchange.getFlowScheduleId());
        dataExchangeEntity.setApproximateVolume(dataExchange.getApproximateVolume());
        dataExchangeEntity.setDataExchangeMethodId(dataExchange.getDataExchangeMethodId());
        dataExchangeEntity.setSecurityInfrastructureId(dataExchange.getSecurityInfrastructureId());
        dataExchangeEntity.setSecurityArchitectureId(dataExchange.getSecurityArchitectureId());
        dataExchangeEntity.setEndpoint(dataExchange.getEndpoint());
        dataExchangeEntity.setFlowStatusId(dataExchange.getFlowStatusId());
        dataExchangeEntity.setUuid(dataExchange.getUuid());
        entityManager.persist(dataExchangeEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void deleteDataExchange(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataExchangeEntity DataExchangeEntity = entityManager.find(DataExchangeEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(DataExchangeEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public List<DataExchangeEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataExchangeEntity> cq = cb.createQuery(DataExchangeEntity.class);
        Root<DataExchangeEntity> rootEntry = cq.from(DataExchangeEntity.class);

        Predicate predicate = cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%");

        cq.where(predicate);
        TypedQuery<DataExchangeEntity> query = entityManager.createQuery(cq);
        List<DataExchangeEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public List<DataExchangeEntity> getDataExchangesFromList(List<String> dataFlows) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataExchangeEntity> cq = cb.createQuery(DataExchangeEntity.class);
        Root<DataExchangeEntity> rootEntry = cq.from(DataExchangeEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(dataFlows);

        cq.where(predicate);
        TypedQuery<DataExchangeEntity> query = entityManager.createQuery(cq);

        List<DataExchangeEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
