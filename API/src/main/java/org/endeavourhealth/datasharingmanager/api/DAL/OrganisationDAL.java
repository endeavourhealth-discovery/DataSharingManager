package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonOrganisation;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonStatistics;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.util.*;

public class OrganisationDAL {

    public void deleteUneditedBulkOrganisations() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery(
                "DELETE from OrganisationEntity o " +
                        "where o.bulkImported = :active " +
                        "and o.bulkItemUpdated = :notActive");
        query.setParameter("active", 1);
        query.setParameter("notActive", 0);

        int deletedCount = query.executeUpdate();

        entityManager.getTransaction().commit();

        System.out.println(deletedCount + " deleted");
        entityManager.close();
    }

    public List<OrganisationEntity> getOrganisations(String expression, boolean searchServices,
                                                            byte organisationType,
                                                            Integer pageNumber, Integer pageSize,
                                                            String orderColumn, boolean descending) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        //Services are just organisations with the isService flag set to true;
        Predicate predicate= cb.equal(rootEntry.get("isService"), (byte) (searchServices ? 1 : 0));

        if (!expression.equals("")){
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

        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);
        query.setFirstResult((pageNumber - 1) * pageSize);
        query.setMaxResults(pageSize);
        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public List<OrganisationEntity> searchOrganisationsFromOdsList(List<String> odsCodes) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        Predicate predicate = rootEntry.get("odsCode").in(odsCodes);

        cq.where(predicate);
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);

        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public void updateOrganisation(JsonOrganisation organisation) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        OrganisationEntity organisationEntity = entityManager.find(OrganisationEntity.class, organisation.getUuid());
        entityManager.getTransaction().begin();
        organisationEntity.setName(organisation.getName());
        organisationEntity.setAlternativeName(organisation.getAlternativeName());
        organisationEntity.setOdsCode(organisation.getOdsCode());
        organisationEntity.setIcoCode(organisation.getIcoCode());
        organisationEntity.setIgToolkitStatus(organisation.getIgToolkitStatus());
        organisationEntity.setIsService((byte) (organisation.getIsService().equals("1") ? 1 : 0));
        organisationEntity.setType(organisation.getType());
        organisationEntity.setBulkItemUpdated((byte)1);
        if (organisation.getDateOfRegistration() != null) {
            organisationEntity.setDateOfRegistration(Date.valueOf(organisation.getDateOfRegistration()));
        }
        //organisationEntity.setRegistrationPerson(organisation.getRegistrationPerson());
        organisationEntity.setEvidenceOfRegistration(organisation.getEvidenceOfRegistration());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void saveOrganisation(JsonOrganisation organisation) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        OrganisationEntity organisationEntity = new OrganisationEntity();
        entityManager.getTransaction().begin();
        organisationEntity.setName(organisation.getName());
        organisationEntity.setAlternativeName(organisation.getAlternativeName());
        organisationEntity.setOdsCode(organisation.getOdsCode());
        organisationEntity.setIcoCode(organisation.getIcoCode());
        organisationEntity.setIgToolkitStatus(organisation.getIgToolkitStatus());
        organisationEntity.setIsService((byte) (organisation.getIsService().equals("1") ? 1 : 0));
        organisationEntity.setBulkImported((byte) (organisation.getBulkImported().equals("1") ? 1 : 0));
        organisationEntity.setBulkItemUpdated((byte) (organisation.getBulkItemUpdated().equals("1") ? 1 : 0));
        organisationEntity.setType(organisation.getType());
        if (organisation.getDateOfRegistration() != null) {
            organisationEntity.setDateOfRegistration(Date.valueOf(organisation.getDateOfRegistration()));
        }
        //organisationEntity.setRegistrationPerson(organisation.getRegistrationPerson());
        organisationEntity.setEvidenceOfRegistration(organisation.getEvidenceOfRegistration());
        organisationEntity.setUuid(organisation.getUuid());
        entityManager.persist(organisationEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void bulkSaveOrganisation(List<OrganisationEntity> organisationEntities) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        int batchSize = 50;

        entityManager.getTransaction().begin();

        for (int i = 0; i < organisationEntities.size(); i++) {
            OrganisationEntity organisationEntity = organisationEntities.get(i);
            entityManager.merge(organisationEntity);
            new AddressDAL().deleteAddressForOrganisations(organisationEntity.getUuid());
            if (i % batchSize == 0){
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void deleteOrganisation(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        OrganisationEntity organisationEntity = entityManager.find(OrganisationEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(organisationEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public Long getTotalNumberOfOrganisations(String expression, boolean searchServices) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);


        //Services are just organisations with the isService flag set to true;
        Predicate predicate= cb.equal(rootEntry.get("isService"), (byte) (searchServices ? 1 : 0));


        if (!expression.equals("")) {
            predicate = cb.and(cb.equal(rootEntry.get("isService"), (byte) (searchServices ? 1 : 0)),
                    (cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                            cb.like(cb.upper(rootEntry.get("odsCode")), "%" + expression.toUpperCase() + "%"),
                            cb.like(cb.upper(rootEntry.get("alternativeName")), "%" + expression.toUpperCase() + "%"),
                            cb.like(cb.upper(rootEntry.get("icoCode")), "%" + expression.toUpperCase() + "%"))));
        }

        cq.select((cb.countDistinct(rootEntry)));

        cq.where(predicate);

        Long ret = entityManager.createQuery(cq).getSingleResult();


        entityManager.close();

        return ret;
    }

    public List<OrganisationEntity> getUpdatedBulkOrganisations() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        Predicate predicate = cb.and(cb.equal(rootEntry.get("bulkImported"), (byte) 1),
                (cb.equal(rootEntry.get("bulkItemUpdated"), (byte) 1)));

        cq.where(predicate);
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);
        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public List<OrganisationEntity> getConflictedOrganisations() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        Predicate predicate = cb.isNotNull(rootEntry.get("bulkConflictedWith"));

        cq.where(predicate);
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);
        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public List<Object[]> executeOrganisationStatisticQuery(String queryName) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        Query query = entityManager.createNamedQuery(queryName);
        List<Object[]> result = query.getResultList();

        entityManager.close();

        return result;
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
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        Predicate predicate = cb.equal(rootEntry.get("type"), orgType);

        cq.where(predicate);
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);
        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
