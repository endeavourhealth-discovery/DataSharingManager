package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDataSet;
import org.endeavourhealth.core.database.dal.usermanager.caching.DataSetCache;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DataSetEntity;
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

    private EntityManager _entityManager;
    private MasterMappingDAL _masterMappingDAL;
    private AuditCompareLogic _auditCompareLogic;
    private UIAuditJDBCDAL _uiAuditJDBCDAL;

    public DatasetDAL() throws Exception {
        _entityManager = ConnectionManager.getDsmEntityManager();
        _masterMappingDAL = new MasterMappingDAL(_entityManager);
        _auditCompareLogic = new AuditCompareLogic();
        _uiAuditJDBCDAL = new UIAuditJDBCDAL();
    }

    private void clearDataSetCache(String dataSetId) throws Exception {
        DataSetCache.clearDataSetCache(dataSetId);
    }

    public List<DataSetEntity> getAllDataSets() throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<DataSetEntity> cq = cb.createQuery(DataSetEntity.class);
            Root<DataSetEntity> rootEntry = cq.from(DataSetEntity.class);
            CriteriaQuery<DataSetEntity> all = cq.select(rootEntry);
            TypedQuery<DataSetEntity> allQuery = _entityManager.createQuery(all);

            List<DataSetEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }

    }

    public void updateDataSet(JsonDataSet dataset, String userProjectId, boolean withMapping) throws Exception {
        DataSetEntity oldDataSetEntity = _entityManager.find(DataSetEntity.class, dataset.getUuid());
        oldDataSetEntity.setMappingsFromDAL();

        try {
            _entityManager.getTransaction().begin();

            DataSetEntity newDataSet = new DataSetEntity(dataset);
            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data set edited", oldDataSetEntity, newDataSet);

            if (withMapping) {
                _masterMappingDAL.updateDataSetMappings(dataset, oldDataSetEntity, auditJson);
            }

            oldDataSetEntity.updateFromJson(dataset);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.EDIT, ItemType.DATASET, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDataSetCache(dataset.getUuid());
    }

    public void saveDataSet(JsonDataSet dataset, String userProjectId) throws Exception {
        DataSetEntity dataSetEntity = new DataSetEntity(dataset);

        try {
            _entityManager.getTransaction().begin();
            _entityManager.persist(dataSetEntity);

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data set created", null, dataSetEntity);

            //_masterMappingDAL.updateDataSetMappings(dataset, null, auditJson);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.ADD, ItemType.DATASET, auditJson);

            _entityManager.getTransaction().commit();

        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDataSetCache(dataset.getUuid());
    }

    public void deleteDataSet(String uuid, String userProjectId) throws Exception {
        try {
            _entityManager.getTransaction().begin();

            DataSetEntity oldDataSetEntity = _entityManager.find(DataSetEntity.class, uuid);
            oldDataSetEntity.setMappingsFromDAL();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data set deleted", oldDataSetEntity, null);
            _masterMappingDAL.updateDataSetMappings(null, oldDataSetEntity, auditJson);
            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.DELETE, ItemType.DATASET, auditJson);

            _entityManager.remove(oldDataSetEntity);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDataSetCache(uuid);
    }

    public List<DataSetEntity> search(String expression) throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<DataSetEntity> cq = cb.createQuery(DataSetEntity.class);
            Root<DataSetEntity> rootEntry = cq.from(DataSetEntity.class);

            Predicate predicate = cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%");

            cq.where(predicate);
            TypedQuery<DataSetEntity> query = _entityManager.createQuery(cq);
            List<DataSetEntity> ret = query.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }
    }
}
