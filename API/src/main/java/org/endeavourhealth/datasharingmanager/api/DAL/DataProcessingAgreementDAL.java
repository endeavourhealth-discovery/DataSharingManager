package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.CohortEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataProcessingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDPA;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.DataProcessingAgreementCache;
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
        oldDPAEntity.setPurposes(new SecurityMasterMappingDAL().getParentMappings(dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PURPOSE.getMapType()));
        oldDPAEntity.setBenefits(new SecurityMasterMappingDAL().getParentMappings(dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.BENEFIT.getMapType()));
        oldDPAEntity.setRegions(new SecurityMasterMappingDAL().getParentMappings(dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.REGION.getMapType()));
        oldDPAEntity.setPublishers(new SecurityMasterMappingDAL().getParentMappings(dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType()));
        oldDPAEntity.setDocumentations(new SecurityMasterMappingDAL().getParentMappings(dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.DOCUMENT.getMapType()));
        DataProcessingAgreementEntity newDPA = new DataProcessingAgreementEntity(dpa);
        JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Data Processing Agreement edited", oldDPAEntity, newDPA);

        try {
            entityManager.getTransaction().begin();
            oldDPAEntity.setName(dpa.getName());
            oldDPAEntity.setDescription(dpa.getDescription());
            oldDPAEntity.setDerivation(dpa.getDerivation());
            oldDPAEntity.setPublisherInformation(dpa.getPublisherInformation());
            oldDPAEntity.setPublisherContractInformation(dpa.getPublisherContractInformation());
            oldDPAEntity.setPublisherDataset(dpa.getPublisherDataset());
            oldDPAEntity.setDsaStatusId(dpa.getDsaStatusId());
            oldDPAEntity.setReturnToSenderPolicy(dpa.getReturnToSenderPolicy());
            if (dpa.getStartDate() != null) {
                oldDPAEntity.setStartDate(Date.valueOf(dpa.getStartDate()));
            }
            if (dpa.getEndDate() != null) {
                oldDPAEntity.setEndDate(Date.valueOf(dpa.getEndDate()));
            }

            auditJson = new MasterMappingDAL().updateDataProcessingAgreementMappings(dpa, oldDPAEntity, auditJson, entityManager);

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
        DataProcessingAgreementEntity dpaEntity = new DataProcessingAgreementEntity();

        dpa.setPurposes(DataSharingAgreementLogic.setUuidsAndSavePurpose(dpa.getPurposes()));
        dpa.setBenefits(DataSharingAgreementLogic.setUuidsAndSavePurpose(dpa.getBenefits()));

        try {
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
            dpaEntity.setUuid(dpa.getUuid());

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

            oldDPAEntity.setPurposes(new SecurityMasterMappingDAL().getChildMappings(uuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PURPOSE.getMapType()));
            oldDPAEntity.setBenefits(new SecurityMasterMappingDAL().getChildMappings(uuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.BENEFIT.getMapType()));

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
