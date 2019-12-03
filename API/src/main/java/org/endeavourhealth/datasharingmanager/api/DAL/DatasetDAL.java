package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DatasetEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDataSet;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.DataSetCache;
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

public class DatasetDAL {

    private void clearDataSetCache(String dataSetId) throws Exception {
        DataSetCache.clearDataSetCache(dataSetId);
    }

    public List<DatasetEntity> getAllDataSets() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DatasetEntity> cq = cb.createQuery(DatasetEntity.class);
            Root<DatasetEntity> rootEntry = cq.from(DatasetEntity.class);
            CriteriaQuery<DatasetEntity> all = cq.select(rootEntry);
            TypedQuery<DatasetEntity> allQuery = entityManager.createQuery(all);

            List<DatasetEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }

    }

    public void updateDataSet(JsonDataSet dataset, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DatasetEntity oldDataSetEntity = entityManager.find(DatasetEntity.class, dataset.getUuid());
        oldDataSetEntity.setDpas(new SecurityMasterMappingDAL().getParentMappings(dataset.getUuid(), MapType.DATASET.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType()));
        DatasetEntity newDataSet = new DatasetEntity(dataset);

        JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Data set edited", oldDataSetEntity, newDataSet);

        try {
            entityManager.getTransaction().begin();
            oldDataSetEntity.setName(dataset.getName());
            oldDataSetEntity.setDescription(dataset.getDescription());
            oldDataSetEntity.setTechnicalDefinition(dataset.getTechnicalDefinition());

            auditJson = new MasterMappingDAL().updateDataSetMappings(dataset, oldDataSetEntity, auditJson, entityManager);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.EDIT, ItemType.DATASET, "bd285adbc36842d7a27088e93c36c13e29ed69fa63a6", null, auditJson);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDataSetCache(dataset.getUuid());
    }

    public void saveDataSet(JsonDataSet dataset, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DatasetEntity dataSetEntity = new DatasetEntity();

        try {
            entityManager.getTransaction().begin();
            dataSetEntity.setUuid(dataset.getUuid());
            dataSetEntity.setName(dataset.getName());
            dataSetEntity.setDescription(dataset.getDescription());
            dataSetEntity.setTechnicalDefinition(dataset.getTechnicalDefinition());

            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Data set created", null, dataSetEntity);

            auditJson = new MasterMappingDAL().updateDataSetMappings(dataset, null, auditJson, entityManager);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.ADD, ItemType.DATASET, null, null, auditJson);


            entityManager.persist(dataSetEntity);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDataSetCache(dataset.getUuid());
    }

    public void deleteDataSet(String uuid, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DatasetEntity oldDataSetEntity = entityManager.find(DatasetEntity.class, uuid);
            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Data set deleted", oldDataSetEntity, null);
            entityManager.getTransaction().begin();
            entityManager.remove(oldDataSetEntity);

            auditJson = new MasterMappingDAL().updateDataSetMappings(null, oldDataSetEntity, auditJson, entityManager);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.DELETE, ItemType.DATASET, null, null, auditJson);


            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDataSetCache(uuid);
    }

    public List<DatasetEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DatasetEntity> cq = cb.createQuery(DatasetEntity.class);
            Root<DatasetEntity> rootEntry = cq.from(DatasetEntity.class);

            Predicate predicate = cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%");

            cq.where(predicate);
            TypedQuery<DatasetEntity> query = entityManager.createQuery(cq);
            List<DatasetEntity> ret = query.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }

    }
}
