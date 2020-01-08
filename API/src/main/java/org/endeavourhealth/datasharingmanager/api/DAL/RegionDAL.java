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

    private void clearRegionCache(String regionID) throws Exception {
        RegionCache.clearRegionCache(regionID);
    }

    public void updateRegion(JsonRegion region, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        RegionEntity oldRegionEntity = entityManager.find(RegionEntity.class, region.getUuid());
        oldRegionEntity.setMappingsFromDAL();

        try {
            entityManager.getTransaction().begin();

            RegionEntity newRegion = new RegionEntity(region);
            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Region edited", oldRegionEntity, newRegion);

            new MasterMappingDAL(entityManager).updateRegionMappings(region, oldRegionEntity, auditJson);

            oldRegionEntity.updateFromJson(region);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId, AuditAction.EDIT, ItemType.REGION, auditJson);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearRegionCache(region.getUuid());
    }

    public void saveRegion(JsonRegion region, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        RegionEntity regionEntity = new RegionEntity(region);

        try {
            entityManager.getTransaction().begin();

            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Region created", null, regionEntity);

            new MasterMappingDAL(entityManager).updateRegionMappings(region, null, auditJson);

            new UIAuditJDBCDAL().addToAuditTrail(userProjectId, AuditAction.ADD, ItemType.REGION, auditJson);

            entityManager.persist(regionEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearRegionCache(region.getUuid());
    }

    public void deleteRegion(String uuid, String userProjectId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            entityManager.getTransaction().begin();

            RegionEntity oldRegionEntity = entityManager.find(RegionEntity.class, uuid);
            oldRegionEntity.setMappingsFromDAL();

            JsonNode auditJson = new AuditCompareLogic().getAuditJsonNode("Region deleted", oldRegionEntity, null);
            new MasterMappingDAL(entityManager).updateRegionMappings(null, oldRegionEntity, auditJson);
            new UIAuditJDBCDAL().addToAuditTrail(userProjectId, AuditAction.DELETE, ItemType.REGION, auditJson);

            entityManager.remove(oldRegionEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

        clearRegionCache(uuid);
    }

    public List<RegionEntity> getRegionsFromList(List<String> regions) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<RegionEntity> cq = cb.createQuery(RegionEntity.class);
            Root<RegionEntity> rootEntry = cq.from(RegionEntity.class);

            Predicate predicate = rootEntry.get("uuid").in(regions);

            cq.where(predicate);
            TypedQuery<RegionEntity> query = entityManager.createQuery(cq);

            List<RegionEntity> ret = query.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }
    }

    public List<RegionEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<RegionEntity> cq = cb.createQuery(RegionEntity.class);
            Root<RegionEntity> rootEntry = cq.from(RegionEntity.class);

            Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                    cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

            cq.where(predicate);
            TypedQuery<RegionEntity> query = entityManager.createQuery(cq);
            List<RegionEntity> ret = query.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }
    }
}
