package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDPA;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDocumentation;
import org.endeavourhealth.core.database.dal.usermanager.caching.DataProcessingAgreementCache;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DataProcessingAgreementEntity;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DocumentationEntity;
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
import java.util.ArrayList;
import java.util.List;

public class DataProcessingAgreementDAL {

    private EntityManager _entityManager;
    private MasterMappingDAL _masterMappingDAL;
    private AuditCompareLogic _auditCompareLogic;
    private UIAuditJDBCDAL _uiAuditJDBCDAL;

    public DataProcessingAgreementDAL() throws Exception {
        _entityManager = ConnectionManager.getDsmEntityManager();
        _masterMappingDAL = new MasterMappingDAL(_entityManager);
        _auditCompareLogic = new AuditCompareLogic();
        _uiAuditJDBCDAL = new UIAuditJDBCDAL();
    }

    private void clearDPACache(String dpaId) throws Exception {
        DataProcessingAgreementCache.clearDataProcessingAgreementCache(dpaId);
    }

    public List<DataProcessingAgreementEntity> getAllDPAs() throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<DataProcessingAgreementEntity> cq = cb.createQuery(DataProcessingAgreementEntity.class);
            Root<DataProcessingAgreementEntity> rootEntry = cq.from(DataProcessingAgreementEntity.class);
            CriteriaQuery<DataProcessingAgreementEntity> all = cq.select(rootEntry);
            TypedQuery<DataProcessingAgreementEntity> allQuery = _entityManager.createQuery(all);
            List<DataProcessingAgreementEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }

    }

    public void updateDPA(JsonDPA dpa, String userProjectId, boolean withMapping) throws Exception {
        DataProcessingAgreementEntity oldDPAEntity = _entityManager.find(DataProcessingAgreementEntity.class, dpa.getUuid());
        oldDPAEntity.setMappingsFromDAL();

        try {
            _entityManager.getTransaction().begin();

            DataProcessingAgreementEntity newDPA = new DataProcessingAgreementEntity(dpa);
            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data Processing Agreement edited", oldDPAEntity, newDPA);

            if (withMapping) {
                _masterMappingDAL.updateDataProcessingAgreementMappings(dpa, oldDPAEntity, auditJson);
            }

            oldDPAEntity.updateFromJson(dpa);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.EDIT, ItemType.DPA, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDPACache(dpa.getUuid());
    }

    public void saveDPA(JsonDPA dpa, String userProjectId) throws Exception {
        DataProcessingAgreementEntity dpaEntity = new DataProcessingAgreementEntity(dpa);

        try {
            _entityManager.getTransaction().begin();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data Processing Agreement created", null, dpaEntity);

            //_masterMappingDAL.updateDataProcessingAgreementMappings(dpa, null, auditJson);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.ADD, ItemType.DPA, auditJson);

            _entityManager.persist(dpaEntity);
            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDPACache(dpa.getUuid());
    }

    public void deleteDPA(String uuid, String userProjectId) throws Exception {
        try {
            _entityManager.getTransaction().begin();

            DataProcessingAgreementEntity oldDPAEntity = _entityManager.find(DataProcessingAgreementEntity.class, uuid);
            oldDPAEntity.setMappingsFromDAL();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data Processing Agreement deleted", oldDPAEntity, null);
            _masterMappingDAL.updateDataProcessingAgreementMappings(null, oldDPAEntity, auditJson);
            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.DELETE, ItemType.DPA, auditJson);

            _entityManager.remove(oldDPAEntity);
            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDPACache(uuid);
    }

    public List<DataProcessingAgreementEntity> search(String expression) throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<DataProcessingAgreementEntity> cq = cb.createQuery(DataProcessingAgreementEntity.class);
            Root<DataProcessingAgreementEntity> rootEntry = cq.from(DataProcessingAgreementEntity.class);

            Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                    cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

            cq.where(predicate);
            TypedQuery<DataProcessingAgreementEntity> query = _entityManager.createQuery(cq);
            List<DataProcessingAgreementEntity> ret = query.getResultList();

            return ret;

        } finally {
            _entityManager.close();
        }
    }

    public void addDocument(String uuid, JsonDocumentation document, String userProjectId) throws Exception {
        DataProcessingAgreementEntity oldDPAEntity = DataProcessingAgreementCache.getDPADetails(uuid);
        oldDPAEntity.setMappingsFromDAL();

        try {
            _entityManager.getTransaction().begin();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data Processing Agreement edited", oldDPAEntity, oldDPAEntity);

            List<JsonDocumentation> documents = new ArrayList();
            JsonDocumentation docJSon = null;
            DocumentationEntity docEntity = null;
            for (String doc : oldDPAEntity.getDocumentations()) {
                if (doc != null) {
                    docEntity = new DocumentationDAL(_entityManager).getDocument(doc);
                    docJSon = new JsonDocumentation();
                    docJSon.setFilename(docEntity.getFilename());
                    docJSon.setFileData(docEntity.getFileData());
                    docJSon.setTitle(docEntity.getTitle());
                    docJSon.setUuid(docEntity.getUuid());
                    documents.add(docJSon);
                }
            }

            JsonDPA dpa = new JsonDPA();
            documents.add(document);
            dpa.setUuid(uuid);
            dpa.setDocumentations(documents);

            _masterMappingDAL.updateDataProcessingAgreementMappings(dpa, oldDPAEntity, auditJson);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.EDIT, ItemType.DPA, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDPACache(uuid);
    }
}
