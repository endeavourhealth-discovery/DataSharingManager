package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.CohortEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonCohort;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.CohortCache;
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

public class CohortDAL {

    private EntityManager _entityManager;
    private MasterMappingDAL _masterMappingDAL;
    private AuditCompareLogic _auditCompareLogic;
    private UIAuditJDBCDAL _uiAuditJDBCDAL;

    public CohortDAL() throws Exception {
        _entityManager = ConnectionManager.getDsmEntityManager();
        _masterMappingDAL = new MasterMappingDAL(_entityManager);
        _auditCompareLogic = new AuditCompareLogic();
        _uiAuditJDBCDAL = new UIAuditJDBCDAL();
    }
    
    private void clearCohortCache(String cohortId) throws Exception {
        CohortCache.clearCohortCache(cohortId);
    }

    public List<CohortEntity> getAllCohorts() {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<CohortEntity> cq = cb.createQuery(CohortEntity.class);
            Root<CohortEntity> rootEntry = cq.from(CohortEntity.class);
            CriteriaQuery<CohortEntity> all = cq.select(rootEntry);
            TypedQuery<CohortEntity> allQuery = _entityManager.createQuery(all);
            List<CohortEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }
    }

    public void updateCohort(JsonCohort cohort, String userProjectId, boolean withMapping) throws Exception {
        CohortEntity oldCohortEntity = _entityManager.find(CohortEntity.class, cohort.getUuid());
        oldCohortEntity.setMappingsFromDAL();

        try {
            _entityManager.getTransaction().begin();

            CohortEntity newCohort = new CohortEntity(cohort);
            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Cohort edited", oldCohortEntity, newCohort);

            if (withMapping) {
                _masterMappingDAL.updateCohortMappings(cohort, oldCohortEntity, auditJson);
            }

            oldCohortEntity.updateFromJson(cohort);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.EDIT, ItemType.COHORT, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearCohortCache(cohort.getUuid());
    }

    public void saveCohort(JsonCohort cohort, String userProjectId) throws Exception {
        CohortEntity cohortEntity = new CohortEntity(cohort);

        try {
            _entityManager.getTransaction().begin();
            _entityManager.persist(cohortEntity);

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Cohort created", null, cohortEntity);

            //_masterMappingDAL.updateCohortMappings(cohort, null, auditJson);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.ADD, ItemType.COHORT, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearCohortCache(cohort.getUuid());
    }

    public void deleteCohort(String uuid, String userProjectId) throws Exception {
        try {
            _entityManager.getTransaction().begin();

            CohortEntity oldCohortEntity = _entityManager.find(CohortEntity.class, uuid);
            oldCohortEntity.setMappingsFromDAL();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Cohort deleted", oldCohortEntity, null);
            _masterMappingDAL.updateCohortMappings(null, oldCohortEntity, auditJson);
            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.DELETE, ItemType.COHORT, auditJson);

            _entityManager.remove(oldCohortEntity);
            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearCohortCache(uuid);
    }

    public List<CohortEntity> search(String expression) throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<CohortEntity> cq = cb.createQuery(CohortEntity.class);
            Root<CohortEntity> rootEntry = cq.from(CohortEntity.class);

            Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                    cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

            cq.where(predicate);
            TypedQuery<CohortEntity> query = _entityManager.createQuery(cq);
            List<CohortEntity> ret = query.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }

    }
}
