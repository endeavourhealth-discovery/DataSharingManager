package org.endeavourhealth.datasharingmanagermodel.models.database;

import org.endeavourhealth.datasharingmanagermodel.PersistenceManager;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonOrganisation;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonStatistics;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.util.*;

@NamedQueries({
        @NamedQuery(name="organisation.total",
                query="select 'Total number of organisations', count(o.uuid) " +
                "from OrganisationEntity o " +
                        "where o.isService = 0"),
        @NamedQuery(name="organisation.bulk",
                query="select 'Bulk imported organisations', count(o.uuid) "  +
                        "from OrganisationEntity o " +
                        "where o.isService = 0 " +
                        "and o.bulkImported = 1"),
        @NamedQuery(name="organisation.editedBulk",
                query="select 'Edited bulk imported organisations', count(o.uuid) " +
                        "from OrganisationEntity o " +
                        "where o.isService = 0 " +
                        "and o.bulkImported = 1 " +
                        "and o.bulkItemUpdated = 1"),
        @NamedQuery(name="organisation.manual",
                query="select 'Manually created organisations', count(o.uuid) " +
                        "from OrganisationEntity o " +
                        "where o.isService = 0 " +
                        "and o.bulkImported = 0 "),
        @NamedQuery(name="region.total",
                query="select 'Total number of regions', count(o.uuid) " +
                        "from RegionEntity o"),
        @NamedQuery(name="region.withDSA",
                query="select 'Regions containing a data sharing agreement', count(distinct r.uuid) " +
                        "from RegionEntity r " +
                        "inner join MasterMappingEntity mm on mm.parentUuid = r.uuid and mm.parentMapTypeId = 2 " +
                        "inner join DataSharingAgreementEntity dsa on dsa.uuid = mm.childUuid and mm.childMapTypeId = 3"),
        @NamedQuery(name="region.withOrganisation",
                query="select 'Regions containing an organisation', count(distinct r.uuid) " +
                        "from RegionEntity r " +
                        "inner join MasterMappingEntity mm on mm.parentUuid = r.uuid and mm.parentMapTypeId = 2 " +
                        "inner join OrganisationEntity o on o.uuid = mm.childUuid and mm.childMapTypeId = 1"),
        @NamedQuery(name="region.withRegion",
                query="select 'Regions containing a region', count(distinct r.uuid) " +
                        "from RegionEntity r " +
                        "inner join MasterMappingEntity mm on mm.parentUuid = r.uuid and mm.parentMapTypeId = 2 " +
                        "inner join RegionEntity cr on cr.uuid = mm.childUuid and mm.childMapTypeId = 2"),
        @NamedQuery(name="region.belongingToRegion",
                query="select 'Regions belonging to a region', count(distinct cr.uuid) " +
                        "from RegionEntity r " +
                        "inner join MasterMappingEntity mm on mm.parentUuid = r.uuid and mm.parentMapTypeId = 2 " +
                        "inner join RegionEntity cr on cr.uuid = mm.childUuid and mm.childMapTypeId = 2"),
        @NamedQuery(name="region.orphaned",
                query="select 'Orphaned regions', count(distinct r.uuid) " +
                        "from RegionEntity r " +
                        "left outer join MasterMappingEntity mmp on mmp.parentUuid = r.uuid and mmp.parentMapTypeId = 2 " +
                        "left outer join MasterMappingEntity mmc on mmc.childUuid= r.uuid and mmc.childMapTypeId = 2 " +
                        "where mmp.parentUuid is null " +
                        "and mmc.parentUuid is null"),
        @NamedQuery(name="service.total",
                query="select 'Total number of services', count(distinct s.uuid) " +
                        "from OrganisationEntity s " +
                        "where s.isService = 1"),
        @NamedQuery(name="service.withOrganisation",
                query="select 'Services linked to an organisation', count(distinct s.uuid) " +
                        "from OrganisationEntity s " +
                        "join MasterMappingEntity mm on mm.childUuid = s.uuid and mm.childMapTypeId = 0 " +
                        "where s.isService = 1"),
        @NamedQuery(name="service.orphaned",
                query="select 'Orphaned services', count(distinct s.uuid) " +
                        "from OrganisationEntity s " +
                        "left outer join MasterMappingEntity mm on mm.childUuid = s.uuid and mm.childMapTypeId = 0 " +
                        "where s.isService = 1 " +
                        "and mm.childUuid is null"),
        @NamedQuery(name="cohort.total",
                query="select 'Total number of base populations', count(distinct c.uuid) " +
                        "from CohortEntity c "),
        @NamedQuery(name="dataFlow.total",
                query="select 'Total number of data flows', count(distinct df.uuid) " +
                        "from DataFlowEntity df "),
        @NamedQuery(name="dataExchange.total",
                query="select 'Total number of data exchanges', count(distinct de.uuid) " +
                        "from DataExchangeEntity de "),
        @NamedQuery(name="dataExchange.totalPubs",
                query="select 'Total number of publisher data exchanges', count(distinct de.uuid) " +
                        "from DataExchangeEntity de " +
                        "where de.publisher = 1  "),
        @NamedQuery(name="dataExchange.totalSubs",
                query="select 'Total number of subscriber data exchanges', count(distinct de.uuid) " +
                        "from DataExchangeEntity de " +
                        "where de.publisher = 0  "),
        @NamedQuery(name="dataExchange.totalVolume",
                query="select 'Total volume for all data flows', coalesce(sum(de.approximateVolume), 0) " +
                        "from DataExchangeEntity de "),
        @NamedQuery(name="dataExchange.averageVolume",
                query="select 'Average volume for data flows', coalesce(avg(de.approximateVolume), 0) " +
                        "from DataExchangeEntity de "),
        @NamedQuery(name="dpa.total",
                query="select 'Total number of data processing agreements', count(distinct dpa.uuid) " +
                        "from DataProcessingAgreementEntity dpa "),
        @NamedQuery(name="dataSet.total",
                query="select 'Total number of datasets', count(distinct ds.uuid) " +
                        "from DatasetEntity ds "),
        @NamedQuery(name="dsa.total",
                query="select 'Total number of data sharing agreements', count(distinct dsa.uuid) " +
                        "from DataSharingAgreementEntity dsa "),
        @NamedQuery(name="dsa.withRegion",
                query="select 'Data sharing agreements belonging to a region', count(distinct dsa.uuid) " +
                        "from DataSharingAgreementEntity dsa " +
                        "inner join MasterMappingEntity mm on mm.childUuid = dsa.uuid and mm.childMapTypeId = 3 " +
                        "inner join RegionEntity r on r.uuid = mm.parentUuid and mm.parentMapTypeId = 2"),
        @NamedQuery(name="dss.total",
                query="select 'Total number of sharing summaries', count(distinct dss.uuid) " +
                        "from DataSharingSummaryEntity dss "),
        @NamedQuery(name="project.total",
                query="select 'Total number of projects', count(distinct p.uuid) " +
                        "from ProjectEntity p "),
})
@Entity
@Table(name = "organisation", schema = "data_sharing_manager")
public class OrganisationEntity {
    private static Map<UUID, List<OrganisationEntity>> cachedOrganisations = new HashMap<>();
    private static Map<UUID, String> cachedSearchTerm = new HashMap<>();
    private String uuid;
    private String name;
    private String alternativeName;
    private String odsCode;
    private String icoCode;
    private String igToolkitStatus;
    private Date dateOfRegistration;
    private String registrationPerson;
    private String evidenceOfRegistration;
    private byte isService;
    private byte bulkImported;
    private byte bulkItemUpdated;
    private String bulkConflictedWith;
    private byte type;
    private byte active;

    @Id
    @Column(name = "uuid", nullable = false, length = 36)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "alternative_name", nullable = true, length = 100)
    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
    }

    @Basic
    @Column(name = "ods_code", nullable = true, length = 10)
    public String getOdsCode() {
        return odsCode;
    }

    public void setOdsCode(String odsCode) {
        this.odsCode = odsCode;
    }

    @Basic
    @Column(name = "ico_code", nullable = true, length = 10)
    public String getIcoCode() {
        return icoCode;
    }

    public void setIcoCode(String icoCode) {
        this.icoCode = icoCode;
    }

    @Basic
    @Column(name = "ig_toolkit_status", nullable = true, length = 10)
    public String getIgToolkitStatus() {
        return igToolkitStatus;
    }

    public void setIgToolkitStatus(String igToolkitStatus) {
        this.igToolkitStatus = igToolkitStatus;
    }

    @Basic
    @Column(name = "date_of_registration", nullable = true)
    public Date getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(Date dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }

    @Basic
    @Column(name = "registration_person", nullable = true, length = 36)
    public String getRegistrationPerson() {
        return registrationPerson;
    }

    public void setRegistrationPerson(String registrationPerson) {
        this.registrationPerson = registrationPerson;
    }

    @Basic
    @Column(name = "evidence_of_registration", nullable = true, length = 500)
    public String getEvidenceOfRegistration() {
        return evidenceOfRegistration;
    }

    public void setEvidenceOfRegistration(String evidenceOfRegistration) {
        this.evidenceOfRegistration = evidenceOfRegistration;
    }

    @Basic
    @Column(name = "is_service", nullable = false)
    public byte getIsService() {
        return isService;
    }

    public void setIsService(byte isService) {
        this.isService = isService;
    }

    @Basic
    @Column(name = "type", nullable = false, length = 40)
    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    @Basic
    @Column(name = "bulk_imported", nullable = false)
    public byte getBulkImported() {
        return bulkImported;
    }

    public void setBulkImported(byte bulkImported) {
        this.bulkImported = bulkImported;
    }

    @Basic
    @Column(name = "bulk_item_updated", nullable = false)
    public byte getBulkItemUpdated() {
        return bulkItemUpdated;
    }

    public void setBulkItemUpdated(byte bulkItemUpdated) {
        this.bulkItemUpdated = bulkItemUpdated;
    }

    @Basic
    @Column(name = "bulk_conflicted_with", nullable = true, length = 36)
    public String getBulkConflictedWith() {
        return bulkConflictedWith;
    }

    public void setBulkConflictedWith(String bulkConflictedWith) {
        this.bulkConflictedWith = bulkConflictedWith;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganisationEntity that = (OrganisationEntity) o;

        if (isService != that.isService) return false;
        if (bulkImported != that.bulkImported) return false;
        if (bulkItemUpdated != that.bulkItemUpdated) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (alternativeName != null ? !alternativeName.equals(that.alternativeName) : that.alternativeName != null)
            return false;
        if (odsCode != null ? !odsCode.equals(that.odsCode) : that.odsCode != null) return false;
        if (icoCode != null ? !icoCode.equals(that.icoCode) : that.icoCode != null) return false;
        if (igToolkitStatus != null ? !igToolkitStatus.equals(that.igToolkitStatus) : that.igToolkitStatus != null)
            return false;
        if (dateOfRegistration != null ? !dateOfRegistration.equals(that.dateOfRegistration) : that.dateOfRegistration != null)
            return false;
        if (registrationPerson != null ? !registrationPerson.equals(that.registrationPerson) : that.registrationPerson != null)
            return false;
        if (evidenceOfRegistration != null ? !evidenceOfRegistration.equals(that.evidenceOfRegistration) : that.evidenceOfRegistration != null)
            return false;
        if (bulkConflictedWith != null ? !bulkConflictedWith.equals(that.bulkConflictedWith) : that.bulkConflictedWith != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (alternativeName != null ? alternativeName.hashCode() : 0);
        result = 31 * result + (odsCode != null ? odsCode.hashCode() : 0);
        result = 31 * result + (icoCode != null ? icoCode.hashCode() : 0);
        result = 31 * result + (igToolkitStatus != null ? igToolkitStatus.hashCode() : 0);
        result = 31 * result + (dateOfRegistration != null ? dateOfRegistration.hashCode() : 0);
        result = 31 * result + (registrationPerson != null ? registrationPerson.hashCode() : 0);
        result = 31 * result + (evidenceOfRegistration != null ? evidenceOfRegistration.hashCode() : 0);
        result = 31 * result + (int) isService;
        result = 31 * result + (int) bulkImported;
        result = 31 * result + (int) bulkItemUpdated;
        result = 31 * result + (bulkConflictedWith != null ? bulkConflictedWith.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "active", nullable = false)
    public byte getActive() {
        return active;
    }

    public void setActive(byte active) {
        this.active = active;
    }

    public static void deleteUneditedBulkOrganisations() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

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

    public static List<OrganisationEntity> getOrganisationsFromList(List<String> organisations) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(organisations);

        cq.where(predicate);
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);

        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<OrganisationEntity> getOrganisations(String expression, boolean searchServices,
                                                            byte organisationType,
                                                            Integer pageNumber, Integer pageSize,
                                                            String orderColumn, boolean descending) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

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



    public static List<OrganisationEntity> searchOrganisations(String expression, boolean searchServices,
                                                            byte organisationType,
                                                            Integer pageNumber, Integer pageSize,
                                                            String orderColumn, boolean descending, UUID userId) throws Exception {

        System.out.println("searching for " +  expression);

        //Only query the DB if the search term has changed for that user
        if (cachedSearchTerm.get(userId) == null || !cachedSearchTerm.get(userId).equals(expression) ) {
            System.out.println("Not found so searching in DB  " + expression);

            cachedOrganisations.remove(userId);
            cachedSearchTerm.put(userId, expression);

            EntityManager entityManager = PersistenceManager.getEntityManager();

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
            Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);


            Predicate predicate = cb.and(cb.equal(rootEntry.get("isService"), (byte) (searchServices ? 1 : 0)),
                    (cb.or(cb.like(rootEntry.get("name"), "%" + expression + "%"),
                            cb.like(rootEntry.get("odsCode"), "%" + expression + "%"),
                            cb.like(rootEntry.get("alternativeName"), "%" + expression + "%"),
                            cb.like(rootEntry.get("icoCode"), "%" + expression + "%"))));

            cq.where(predicate);

            TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);

            cachedOrganisations.put(userId, query.getResultList());

            entityManager.close();
        }

        List<OrganisationEntity> cachedOrgs = cachedOrganisations.get(userId);
        System.out.println("found " + cachedOrgs.size() + " orgs");

        if (cachedOrgs != null) {
            sortOrganisationCache(cachedOrganisations.get(userId), orderColumn, descending);
            return paginateOrganisationCache(cachedOrganisations.get(userId), pageNumber, pageSize);
        }

        return Collections.emptyList();
    }

    private static void sortOrganisationCache(List<OrganisationEntity> orgs, String orderColumn, boolean descending) throws Exception {
        switch (orderColumn) {
            case "name":
                orgs.sort(Comparator.comparing(OrganisationEntity::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "odsCode":
                orgs.sort(Comparator.comparing(OrganisationEntity::getOdsCode, String.CASE_INSENSITIVE_ORDER));
                break;
            default: throw new Exception("Order column not recognised");
        }

        if (descending) {
            Collections.reverse(orgs);
        }

    }

    private static List<OrganisationEntity> paginateOrganisationCache(List<OrganisationEntity> orgs, Integer pageNumber, Integer pageSize) {
        if(pageSize <= 0 || pageNumber <= 0) {
            throw new IllegalArgumentException("invalid page size: " + pageSize);
        }

        int fromIndex = (pageNumber - 1) * pageSize;
        if(orgs == null || orgs.size() < fromIndex){
            return Collections.emptyList();
        }

        return orgs.subList(fromIndex, Math.min(fromIndex + pageSize, orgs.size()));
    }

    public static OrganisationEntity getOrganisation(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        OrganisationEntity ret = entityManager.find(OrganisationEntity.class, uuid);

        entityManager.close();

        return ret;
    }

    public static void updateOrganisation(JsonOrganisation organisation) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

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

    public static void saveOrganisation(JsonOrganisation organisation) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

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

    public static void bulkSaveOrganisation(List<OrganisationEntity> organisationEntities) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        int batchSize = 50;

        entityManager.getTransaction().begin();

        for (int i = 0; i < organisationEntities.size(); i++) {
            OrganisationEntity organisationEntity = organisationEntities.get(i);
            entityManager.merge(organisationEntity);
            AddressEntity.deleteAddressForOrganisations(organisationEntity.uuid);
            if (i % batchSize == 0){
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteOrganisation(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        OrganisationEntity organisationEntity = entityManager.find(OrganisationEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(organisationEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static Long getTotalNumberOfOrganisations(String expression, boolean searchServices) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

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

    public static List<OrganisationEntity> getUpdatedBulkOrganisations() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

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

    public static List<OrganisationEntity> getConflictedOrganisations() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

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

    public static List<Object[]> executeOrganisationStatisticQuery(String queryName) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        Query query = entityManager.createNamedQuery(queryName);
        List<Object[]> result = query.getResultList();

        entityManager.close();

        return result;
    }

    public static List<JsonStatistics> getStatisticsForType(String type) throws Exception {
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

    private static List<String> getStatisticsQueries(String type) throws Exception {
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

    private static List<String> getOrganisationStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("organisation.total");
        queryNames.add("organisation.bulk");
        queryNames.add("organisation.editedBulk");
        queryNames.add("organisation.manual");
        return queryNames;
    }

    private static List<String> getRegionStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("region.total");
        queryNames.add("region.withDSA");
        queryNames.add("region.withOrganisation");
        queryNames.add("region.withRegion");
        queryNames.add("region.belongingToRegion");
        queryNames.add("region.orphaned");
        return queryNames;
    }

    private static List<String> getServiceStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("service.total");
        queryNames.add("service.withOrganisation");
        queryNames.add("service.orphaned");
        return queryNames;
    }

    private static List<String> getCohortStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("cohort.total");
        return queryNames;
    }

    private static List<String> getDataFlowStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dataFlow.total");
        return queryNames;
    }

    private static List<String> getDataExchangeStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dataExchange.total");
        queryNames.add("dataExchange.totalPubs");
        queryNames.add("dataExchange.totalSubs");
        queryNames.add("dataExchange.totalVolume");
        queryNames.add("dataExchange.averageVolume");
        return queryNames;
    }

    private static List<String> getDPAStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dpa.total");
        return queryNames;
    }

    private static List<String> getDataSetStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dataSet.total");
        return queryNames;
    }

    private static List<String> getDSAStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dsa.total");
        queryNames.add("dsa.withRegion");
        return queryNames;
    }

    private static List<String> getDSSStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("dss.total");
        return queryNames;
    }

    private static List<String> getProjectStatisticsQueries() throws Exception {
        List<String> queryNames = new ArrayList<>();
        queryNames.add("project.total");
        return queryNames;
    }

    public static List<OrganisationEntity> getOrganisationByType(byte orgType) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

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
