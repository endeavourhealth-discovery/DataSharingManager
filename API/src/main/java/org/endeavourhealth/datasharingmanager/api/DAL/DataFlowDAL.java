package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataFlowEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDataFlow;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class DataFlowDAL {

    public List<DataFlowEntity> getAllDataFlows() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataFlowEntity> cq = cb.createQuery(DataFlowEntity.class);
        Root<DataFlowEntity> rootEntry = cq.from(DataFlowEntity.class);
        CriteriaQuery<DataFlowEntity> all = cq.select(rootEntry);
        TypedQuery<DataFlowEntity> allQuery = entityManager.createQuery(all);
        List<DataFlowEntity> ret =  allQuery.getResultList();

        entityManager.close();

        return ret;
    }

    public DataFlowEntity getDataFlow(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataFlowEntity ret = entityManager.find(DataFlowEntity.class, uuid);

        entityManager.close();

        return ret;
    }

    public void updateDataFlow(JsonDataFlow dataFlow) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataFlowEntity dataFlowEntity = entityManager.find(DataFlowEntity.class, dataFlow.getUuid());
        entityManager.getTransaction().begin();
        dataFlowEntity.setName(dataFlow.getName());
        dataFlowEntity.setStorageProtocolId(dataFlow.getStorageProtocolId());
        dataFlowEntity.setDeidentificationLevel(dataFlow.getDeidentificationLevel());
        dataFlowEntity.setConsentModelId(dataFlow.getConsentModelId());
        dataFlowEntity.setPurpose(dataFlow.getPurpose());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void saveDataFlow(JsonDataFlow dataFlow) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataFlowEntity dataFlowEntity = new DataFlowEntity();
        entityManager.getTransaction().begin();
        dataFlowEntity.setName(dataFlow.getName());
        dataFlowEntity.setStorageProtocolId(dataFlow.getStorageProtocolId());
        dataFlowEntity.setDeidentificationLevel(dataFlow.getDeidentificationLevel());
        dataFlowEntity.setConsentModelId(dataFlow.getConsentModelId());
        dataFlowEntity.setPurpose(dataFlow.getPurpose());
        dataFlowEntity.setUuid(dataFlow.getUuid());
        entityManager.persist(dataFlowEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void deleteDataFlow(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DataFlowEntity dataFlowEntity = entityManager.find(DataFlowEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(dataFlowEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public List<DataFlowEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataFlowEntity> cq = cb.createQuery(DataFlowEntity.class);
        Root<DataFlowEntity> rootEntry = cq.from(DataFlowEntity.class);

        Predicate predicate = cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%");

        cq.where(predicate);
        TypedQuery<DataFlowEntity> query = entityManager.createQuery(cq);
        List<DataFlowEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public List<DataFlowEntity> getDataFlowsFromList(List<String> dataFlows) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataFlowEntity> cq = cb.createQuery(DataFlowEntity.class);
        Root<DataFlowEntity> rootEntry = cq.from(DataFlowEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(dataFlows);

        cq.where(predicate);
        TypedQuery<DataFlowEntity> query = entityManager.createQuery(cq);

        List<DataFlowEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
