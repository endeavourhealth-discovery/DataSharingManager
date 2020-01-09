package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataSharingAgreementEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.RegionEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonRegion;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.RegionCache;
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

public class RegionDAL {

    private EntityManager _entityManager;
    private MasterMappingDAL _masterMappingDAL;
    private AuditCompareLogic _auditCompareLogic;
    private UIAuditJDBCDAL _uiAuditJDBCDAL;

    public RegionDAL() throws Exception {
        _entityManager = ConnectionManager.getDsmEntityManager();
        _masterMappingDAL = new MasterMappingDAL(_entityManager);
        _auditCompareLogic = new AuditCompareLogic();
        _uiAuditJDBCDAL = new UIAuditJDBCDAL();
    }

    private void clearRegionCache(String regionID) throws Exception {
        RegionCache.clearRegionCache(regionID);
    }

    public void updateRegion(JsonRegion region, String userProjectId) throws Exception {
        RegionEntity oldRegionEntity = _entityManager.find(RegionEntity.class, region.getUuid());
        oldRegionEntity.setMappingsFromDAL();

        try {
            _entityManager.getTransaction().begin();

            RegionEntity newRegion = new RegionEntity(region);
            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Region edited", oldRegionEntity, newRegion);

            _masterMappingDAL.updateRegionMappings(region, oldRegionEntity, auditJson);

            oldRegionEntity.updateFromJson(region);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.EDIT, ItemType.REGION, auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearRegionCache(region.getUuid());
    }

    public void saveRegion(JsonRegion region, String userProjectId) throws Exception {
        RegionEntity regionEntity = new RegionEntity(region);

        try {
            _entityManager.getTransaction().begin();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Region created", null, regionEntity);

            _masterMappingDAL.updateRegionMappings(region, null, auditJson);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.ADD, ItemType.REGION, auditJson);

            _entityManager.persist(regionEntity);
            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearRegionCache(region.getUuid());
    }

    public void deleteRegion(String uuid, String userProjectId) throws Exception {
        try {
            _entityManager.getTransaction().begin();

            RegionEntity oldRegionEntity = _entityManager.find(RegionEntity.class, uuid);
            oldRegionEntity.setMappingsFromDAL();

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode("Region deleted", oldRegionEntity, null);
            _masterMappingDAL.updateRegionMappings(null, oldRegionEntity, auditJson);
            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.DELETE, ItemType.REGION, auditJson);

            _entityManager.remove(oldRegionEntity);
            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearRegionCache(uuid);
    }

    public List<RegionEntity> getRegionsFromList(List<String> regions) throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<RegionEntity> cq = cb.createQuery(RegionEntity.class);
            Root<RegionEntity> rootEntry = cq.from(RegionEntity.class);

            Predicate predicate = rootEntry.get("uuid").in(regions);

            cq.where(predicate);
            TypedQuery<RegionEntity> query = _entityManager.createQuery(cq);

            List<RegionEntity> ret = query.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }
    }

    public List<RegionEntity> search(String expression) throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<RegionEntity> cq = cb.createQuery(RegionEntity.class);
            Root<RegionEntity> rootEntry = cq.from(RegionEntity.class);

            Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                    cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

            cq.where(predicate);
            TypedQuery<RegionEntity> query = _entityManager.createQuery(cq);
            List<RegionEntity> ret = query.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }
    }
}
