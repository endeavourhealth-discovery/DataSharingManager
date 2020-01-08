package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataSharingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDSA;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.DataSharingAgreementCache;
import org.endeavourhealth.datasharingmanager.api.Logic.DataSharingAgreementLogic;
import org.endeavourhealth.uiaudit.dal.UIAuditJDBCDAL;
import org.endeavourhealth.uiaudit.enums.AuditAction;
import org.endeavourhealth.uiaudit.enums.ItemType;
import org.endeavourhealth.uiaudit.logic.AuditCompareLogic;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class DataSharingAgreementDAL {

    private EntityManager _entityManager;
    private MasterMappingDAL _masterMappingDAL;
    private AuditCompareLogic _auditCompareLogic;
    private UIAuditJDBCDAL _uiAuditJDBCDAL;

    public DataSharingAgreementDAL() throws Exception {
        _entityManager = ConnectionManager.getDsmEntityManager();
        _masterMappingDAL = new MasterMappingDAL(_entityManager);
        _auditCompareLogic = new AuditCompareLogic();
        _uiAuditJDBCDAL = new UIAuditJDBCDAL();
    }

    private void clearDSACache(String dsaId) throws Exception {
        DataSharingAgreementCache.clearDataSharingAgreementCache(dsaId);
    }

    public List<DataSharingAgreementEntity> getAllDSAs() throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<DataSharingAgreementEntity> cq = cb.createQuery(DataSharingAgreementEntity.class);
            Root<DataSharingAgreementEntity> rootEntry = cq.from(DataSharingAgreementEntity.class);
            CriteriaQuery<DataSharingAgreementEntity> all = cq.select(rootEntry);
            TypedQuery<DataSharingAgreementEntity> allQuery = _entityManager.createQuery(all);
            List<DataSharingAgreementEntity> ret = allQuery.getResultList();

            return ret;

        } finally {
            _entityManager.close();
        }

    }

    public void updateDSA(JsonDSA dsa, String userProjectId) throws Exception {
        DataSharingAgreementEntity oldDSAEntity = _entityManager.find(DataSharingAgreementEntity.class, dsa.getUuid());
        oldDSAEntity.setMappingsFromDAL();

        try {
            _entityManager.getTransaction().begin();

            dsa.setPurposes(DataSharingAgreementLogic.setUuidsAndSavePurpose(dsa.getPurposes(), _entityManager));
            dsa.setBenefits(DataSharingAgreementLogic.setUuidsAndSavePurpose(dsa.getBenefits(), _entityManager));
            
            DataSharingAgreementEntity newDSA = new DataSharingAgreementEntity(dsa);
            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data Sharing Agreement edited", oldDSAEntity, newDSA);

            _masterMappingDAL.updateDataSharingAgreementMappings(dsa, oldDSAEntity, auditJson);
            
            oldDSAEntity.updateFromJson(dsa);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.EDIT, ItemType.DSA, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDSACache(dsa.getUuid());
    }

    public void saveDSA(JsonDSA dsa, String userProjectId) throws Exception {
        DataSharingAgreementEntity dsaEntity = new DataSharingAgreementEntity(dsa);

        try {
            _entityManager.getTransaction().begin();

            dsa.setPurposes(DataSharingAgreementLogic.setUuidsAndSavePurpose(dsa.getPurposes(), _entityManager));
            dsa.setBenefits(DataSharingAgreementLogic.setUuidsAndSavePurpose(dsa.getBenefits(), _entityManager));

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data Sharing Agreement created", null, dsaEntity);

            _masterMappingDAL.updateDataSharingAgreementMappings(dsa, null, auditJson);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.ADD, ItemType.DSA, auditJson);

            _entityManager.persist(dsaEntity);
            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDSACache(dsa.getUuid());
    }

    public void deleteDSA(String uuid, String userProjectId) throws Exception {
        try {
            _entityManager.getTransaction().begin();

            DataSharingAgreementEntity oldDSAEntity = _entityManager.find(DataSharingAgreementEntity.class, uuid);
            oldDSAEntity.setMappingsFromDAL();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Data Sharing Agreement deleted", oldDSAEntity, null);
            _masterMappingDAL.updateDataSharingAgreementMappings(null, oldDSAEntity, auditJson);
            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.DELETE, ItemType.DSA, auditJson);

            _entityManager.remove(oldDSAEntity);
            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearDSACache(uuid);
    }

    public List<DataSharingAgreementEntity> search(String expression) throws Exception {
        try {

            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<DataSharingAgreementEntity> cq = cb.createQuery(DataSharingAgreementEntity.class);
            Root<DataSharingAgreementEntity> rootEntry = cq.from(DataSharingAgreementEntity.class);

            Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                    cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

            cq.where(predicate);
            TypedQuery<DataSharingAgreementEntity> query = _entityManager.createQuery(cq);
            List<DataSharingAgreementEntity> ret = query.getResultList();

            return ret;

        } finally {
            _entityManager.close();
        }

    }

    public List<String> checkDataSharingAgreementsForOrganisation(String odsCode) throws Exception {
        try {

            Query query = _entityManager.createQuery(
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
            _entityManager.close();
        }
    }
}
