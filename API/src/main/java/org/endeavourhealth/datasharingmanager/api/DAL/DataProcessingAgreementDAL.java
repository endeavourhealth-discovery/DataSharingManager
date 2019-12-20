package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.CohortEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataProcessingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDPA;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.DataProcessingAgreementCache;
import org.endeavourhealth.datasharingmanager.api.Logic.DataSharingAgreementLogic;
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

    public void updateDPA(JsonDPA dpa, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        dpa.setPurposes(DataSharingAgreementLogic.setUuidsAndSavePurpose(dpa.getPurposes()));
        dpa.setBenefits(DataSharingAgreementLogic.setUuidsAndSavePurpose(dpa.getBenefits()));

        DataProcessingAgreementEntity oldDPAEntity = entityManager.find(DataProcessingAgreementEntity.class, dpa.getUuid());
        oldDPAEntity.setMappingsFromDAL();

        DataProcessingAgreementEntity newDPA = new DataProcessingAgreementEntity(dpa);
        JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Data Processing Agreement edited", oldDPAEntity, newDPA);

        try {
            entityManager.getTransaction().begin();

            auditJson = new MasterMappingDAL().updateDataProcessingAgreementMappings(dpa, oldDPAEntity, auditJson, entityManager);

            oldDPAEntity.updateFromJson(dpa);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.EDIT, ItemType.DPA, null, null, auditJson);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearDPACache(dpa.getUuid());
    }

    public void saveDPA(JsonDPA dpa, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        DataProcessingAgreementEntity dpaEntity = new DataProcessingAgreementEntity(dpa);

        dpa.setPurposes(DataSharingAgreementLogic.setUuidsAndSavePurpose(dpa.getPurposes()));
        dpa.setBenefits(DataSharingAgreementLogic.setUuidsAndSavePurpose(dpa.getBenefits()));

        try {
            entityManager.getTransaction().begin();

            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Data Processing Agreement created", null, dpaEntity);

            auditJson = new MasterMappingDAL().updateDataProcessingAgreementMappings(dpa, null, auditJson, entityManager);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.ADD, ItemType.DPA, null, null, auditJson);

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

    public void deleteDPA(String uuid, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            entityManager.getTransaction().begin();

            DataProcessingAgreementEntity oldDPAEntity = entityManager.find(DataProcessingAgreementEntity.class, uuid);
            oldDPAEntity.setMappingsFromDAL();

            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Data Processing Agreement deleted", oldDPAEntity, null);
            auditJson = new MasterMappingDAL().updateDataProcessingAgreementMappings(null, oldDPAEntity, auditJson, entityManager);
            new UIAuditJDBCDAL().addToAuditTrail(userProjectId,
                    AuditAction.DELETE, ItemType.DPA, null, null, auditJson);

            entityManager.remove(oldDPAEntity);
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
