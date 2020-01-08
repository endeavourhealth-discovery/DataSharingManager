package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.MasterMappingEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonOrganisation;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonStatistics;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.OrganisationCache;
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
import java.util.*;

public class OrganisationDAL {

    private EntityManager _entityManager;
    private AddressDAL _addressDAL;
    private MasterMappingDAL _masterMappingDAL;
    private AuditCompareLogic _auditCompareLogic;
    private UIAuditJDBCDAL _uiAuditJDBCDAL;

    public OrganisationDAL() throws Exception {
        _entityManager = ConnectionManager.getDsmEntityManager();
        _addressDAL = new AddressDAL();
        _masterMappingDAL = new MasterMappingDAL(_entityManager);
        _auditCompareLogic = new AuditCompareLogic();
        _uiAuditJDBCDAL = new UIAuditJDBCDAL();
    }

    private void clearOrganisationCache(String organisationId) throws Exception {
        OrganisationCache.clearOrganisationCache(organisationId);
    }

    public void deleteUneditedBulkOrganisations() throws Exception {
        try {
            _entityManager.getTransaction().begin();
            Query query = _entityManager.createQuery(
                    "DELETE from OrganisationEntity o " +
                            "where o.bulkImported = :active " +
                            "and o.bulkItemUpdated = :notActive");
            query.setParameter("active", 1);
            query.setParameter("notActive", 0);

            int deletedCount = query.executeUpdate();

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }
    }

    public List<OrganisationEntity> getOrganisations(String expression, boolean searchServices,
                                                     byte organisationType,
                                                     Integer pageNumber, Integer pageSize,
                                                     String orderColumn, boolean descending) throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
            Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

            //Services are just organisations with the isService flag set to true;
            Predicate predicate = cb.equal(rootEntry.get("isService"), (byte) (searchServices ? 1 : 0));

            if (!expression.equals("")) {
                predicate = cb.and(cb.equal(rootEntry.get("isService"), (byte) (searchServices ? 1 : 0)),
                        (cb.or(cb.like(rootEntry.get("name"), "%" + expression + "%"),
                                cb.like(rootEntry.get("odsCode"), "%" + expression + "%"),
                                cb.like(rootEntry.get("alternativeName"), "%" + expression + "%"),
                                cb.like(rootEntry.get("icoCode"), "%" + expression + "%"))));
            }

            if (descending)
                cq.where(predicate).orderBy(cb.desc(rootEntry.get(orderColumn)), cb.desc(rootEntry.get("uuid")));
            else
                cq.where(predicate).orderBy(cb.asc(rootEntry.get(orderColumn)), cb.asc(rootEntry.get("uuid")));

            TypedQuery<OrganisationEntity> query = _entityManager.createQuery(cq);
            query.setFirstResult((pageNumber - 1) * pageSize);
            query.setMaxResults(pageSize);
            List<OrganisationEntity> ret = query.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }
    }

    public void updateOrganisation(JsonOrganisation organisation, String userProjectId) throws Exception {
        OrganisationEntity oldOrganisationEntity = _entityManager.find(OrganisationEntity.class, organisation.getUuid());
        oldOrganisationEntity.setMappingsFromDAL();
        oldOrganisationEntity.setAddresses(_addressDAL.getAddressesForOrganisation(organisation.getUuid()));

        try {
            _entityManager.getTransaction().begin();

            OrganisationEntity newOrganisation = new OrganisationEntity(organisation);
            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode(newOrganisation.organisationOrService() + " edited", oldOrganisationEntity, newOrganisation);

            _addressDAL.updateAddressesAndGetAudit(organisation.getAddresses(), oldOrganisationEntity.getAddresses(), organisation.getUuid(), auditJson, _entityManager);
            _masterMappingDAL.updateOrganisationMappings(organisation, oldOrganisationEntity, auditJson);

            oldOrganisationEntity.updateFromJson(organisation);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.EDIT, (newOrganisation.getIsService() == 1 ? ItemType.SERVICE : ItemType.ORGANISATION), auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearOrganisationCache(organisation.getUuid());
    }

    public void saveOrganisation(JsonOrganisation organisation, String userProjectId) throws Exception {
        OrganisationEntity organisationEntity = new OrganisationEntity(organisation);

        try {
            _entityManager.getTransaction().begin();
            _entityManager.persist(organisationEntity);

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode(organisationEntity.organisationOrService() + " created", null, organisationEntity);

            _addressDAL.updateAddressesAndGetAudit(organisation.getAddresses(), null, organisation.getUuid(), auditJson, _entityManager);
            _masterMappingDAL.updateOrganisationMappings(organisation, null, auditJson);

            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.ADD, (organisationEntity.getIsService() == 1 ? ItemType.SERVICE : ItemType.ORGANISATION), auditJson);

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearOrganisationCache(organisation.getUuid());
    }

    public void bulkSaveOrganisation(List<OrganisationEntity> organisationEntities) throws Exception {
        try {
            int batchSize = 50;

            _entityManager.getTransaction().begin();

            for (int i = 0; i < organisationEntities.size(); i++) {
                OrganisationEntity organisationEntity = organisationEntities.get(i);
                _entityManager.merge(organisationEntity);
                new AddressDAL().deleteAddressForOrganisations(organisationEntity.getUuid());
                if (i % batchSize == 0) {
                    _entityManager.flush();
                    _entityManager.clear();
                }
            }

            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }
    }

    public void deleteOrganisation(String uuid, String userProjectId) throws Exception {
        try {
            _entityManager.getTransaction().begin();

            OrganisationEntity oldOrganisationEntity = _entityManager.find(OrganisationEntity.class, uuid);
            oldOrganisationEntity.setMappingsFromDAL();
            oldOrganisationEntity.setAddresses(_addressDAL.getAddressesForOrganisation(uuid));

            JsonNode auditJson = _auditCompareLogic.getAuditJsonNode(oldOrganisationEntity.organisationOrService() + " deleted", oldOrganisationEntity, null);
            _addressDAL.updateAddressesAndGetAudit(null, oldOrganisationEntity.getAddresses(), oldOrganisationEntity.getUuid(), auditJson, _entityManager);
            _masterMappingDAL.updateOrganisationMappings(null, oldOrganisationEntity, auditJson);
            _uiAuditJDBCDAL.addToAuditTrail(userProjectId, AuditAction.DELETE, (oldOrganisationEntity.getIsService() == 1 ? ItemType.SERVICE : ItemType.ORGANISATION), auditJson);

            _entityManager.remove(oldOrganisationEntity);
            _entityManager.getTransaction().commit();
        } catch (Exception e) {
            _entityManager.getTransaction().rollback();
            throw e;
        } finally {
            _entityManager.close();
        }

        clearOrganisationCache(uuid);
    }

    public Long getTotalNumberOfOrganisations(String expression, boolean searchServices) throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);


            //Services are just organisations with the isService flag set to true;
            Predicate predicate = cb.equal(rootEntry.get("isService"), (byte) (searchServices ? 1 : 0));


            if (!expression.equals("")) {
                predicate = cb.and(cb.equal(rootEntry.get("isService"), (byte) (searchServices ? 1 : 0)),
                        (cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                                cb.like(cb.upper(rootEntry.get("odsCode")), "%" + expression.toUpperCase() + "%"),
                                cb.like(cb.upper(rootEntry.get("alternativeName")), "%" + expression.toUpperCase() + "%"),
                                cb.like(cb.upper(rootEntry.get("icoCode")), "%" + expression.toUpperCase() + "%"))));
            }

            cq.select((cb.countDistinct(rootEntry)));

            cq.where(predicate);

            Long ret = _entityManager.createQuery(cq).getSingleResult();

            return ret;

        } finally {
            _entityManager.close();
        }
    }

    public List<OrganisationEntity> getUpdatedBulkOrganisations() throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
            Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

            Predicate predicate = cb.and(cb.equal(rootEntry.get("bulkImported"), (byte) 1),
                    (cb.equal(rootEntry.get("bulkItemUpdated"), (byte) 1)));

            cq.where(predicate);
            TypedQuery<OrganisationEntity> query = _entityManager.createQuery(cq);
            List<OrganisationEntity> ret = query.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }
    }

    public List<OrganisationEntity> getConflictedOrganisations() throws Exception {
        try {

            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
            Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

            Predicate predicate = cb.isNotNull(rootEntry.get("bulkConflictedWith"));

            cq.where(predicate);
            TypedQuery<OrganisationEntity> query = _entityManager.createQuery(cq);
            List<OrganisationEntity> ret = query.getResultList();

            return ret;
        } finally {
            _entityManager.close();
        }
    }

    public List<Object[]> executeOrganisationStatisticQuery(String queryName) throws Exception {
        try {
            Query query = _entityManager.createNamedQuery(queryName);
            List<Object[]> result = query.getResultList();

            return result;
        } finally {
            _entityManager.close();
        }

    }

    public List<JsonStatistics> getStatisticsForType(String type) throws Exception {
        List<JsonStatistics> statsList = new ArrayList<>();

        List<String> queryNames = getStatisticsQueries(type);

        for (String queryName : queryNames) {
            JsonStatistics jsonStats = new JsonStatistics();
            List<Object[]> result = executeOrganisationStatisticQuery(queryName);
            jsonStats.setLabel(result.get(0)[0].toString());
            jsonStats.setValue(result.get(0)[1].toString());

            statsList.add(jsonStats);
        }
        return statsList;
    }

    private List<String> getStatisticsQueries(String type) throws Exception {
        switch (type) {
            case "organisation":
                return getOrganisationStatisticsQueries();
            case "region":
                return getRegionStatisticsQueries();
            case "service":
                return getServiceStatisticsQueries();
            case "cohort":
                return getCohortStatisticsQueries();
            case "dataflow":
                return getDataFlowStatisticsQueries();
            case "dpa":
                return getDPAStatisticsQueries();
            case "dataset":
                return getDataSetStatisticsQueries();
            case "dsa":
                return getDSAStatisticsQueries();
            case "exchange":
                return getDataExchangeStatisticsQueries();
            case "summary":
                return getDSSStatisticsQueries();
            case "project":
                return getProjectStatisticsQueries();
            default:
                return getOrganisationStatisticsQueries();
        }
    }

    private List<String> getOrganisationStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("organisation.total");
        queryNames.add("organisation.bulk");
        queryNames.add("organisation.editedBulk");
        queryNames.add("organisation.manual");
        return queryNames;
    }

    private List<String> getRegionStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("region.total");
        queryNames.add("region.withDSA");
        queryNames.add("region.withOrganisation");
        queryNames.add("region.withRegion");
        queryNames.add("region.belongingToRegion");
        queryNames.add("region.orphaned");
        return queryNames;
    }

    private List<String> getServiceStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("service.total");
        queryNames.add("service.withOrganisation");
        queryNames.add("service.orphaned");
        return queryNames;
    }

    private List<String> getCohortStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("cohort.total");
        return queryNames;
    }

    private List<String> getDataFlowStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dataFlow.total");
        return queryNames;
    }

    private List<String> getDataExchangeStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dataExchange.total");
        queryNames.add("dataExchange.totalPubs");
        queryNames.add("dataExchange.totalSubs");
        queryNames.add("dataExchange.totalVolume");
        queryNames.add("dataExchange.averageVolume");
        return queryNames;
    }

    private List<String> getDPAStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dpa.total");
        return queryNames;
    }

    private List<String> getDataSetStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dataSet.total");
        return queryNames;
    }

    private List<String> getDSAStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dsa.total");
        queryNames.add("dsa.withRegion");
        return queryNames;
    }

    private List<String> getDSSStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dss.total");
        return queryNames;
    }

    private List<String> getProjectStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("project.total");
        return queryNames;
    }

    public List<OrganisationEntity> getOrganisationByType(byte orgType) throws Exception {
        try {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
            Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

            Predicate predicate = cb.equal(rootEntry.get("type"), orgType);

            cq.where(predicate);
            TypedQuery<OrganisationEntity> query = _entityManager.createQuery(cq);
            List<OrganisationEntity> ret = query.getResultList();

            return ret;

        } finally {
            _entityManager.close();
        }
    }
}
