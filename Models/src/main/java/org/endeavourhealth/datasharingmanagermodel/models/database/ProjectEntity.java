package org.endeavourhealth.datasharingmanagermodel.models.database;

import org.endeavourhealth.datasharingmanagermodel.PersistenceManager;
import org.endeavourhealth.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonProject;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Entity
@Table(name = "project", schema = "data_sharing_manager")
public class ProjectEntity {
    private String uuid;
    private String name;
    private String leadUser;
    private String technicalLeadUser;
    private Short consentModelId;
    private Short deidentificationLevel;
    private Short projectTypeId;
    private Short securityInfrastructureId;
    private String ipAddress;
    private String summary;
    private String businessCase;
    private String objectives;
    private short securityArchitectureId;
    private short storageProtocolId;

    @Id
    @Column(name = "uuid")
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "lead_user")
    public String getLeadUser() {
        return leadUser;
    }

    public void setLeadUser(String leadUser) {
        this.leadUser = leadUser;
    }

    @Basic
    @Column(name = "technical_lead_user")
    public String getTechnicalLeadUser() {
        return technicalLeadUser;
    }

    public void setTechnicalLeadUser(String technicalLeadUser) {
        this.technicalLeadUser = technicalLeadUser;
    }

    @Basic
    @Column(name = "consent_model_id")
    public Short getConsentModelId() {
        return consentModelId;
    }

    public void setConsentModelId(Short consentModelId) {
        this.consentModelId = consentModelId;
    }

    @Basic
    @Column(name = "deidentification_level")
    public Short getDeidentificationLevel() {
        return deidentificationLevel;
    }

    public void setDeidentificationLevel(Short deidentificationLevel) {
        this.deidentificationLevel = deidentificationLevel;
    }

    @Basic
    @Column(name = "project_type_id")
    public Short getProjectTypeId() {
        return projectTypeId;
    }

    public void setProjectTypeId(Short projectTypeId) {
        this.projectTypeId = projectTypeId;
    }

    @Basic
    @Column(name = "security_infrastructure_id")
    public Short getSecurityInfrastructureId() {
        return securityInfrastructureId;
    }

    public void setSecurityInfrastructureId(Short securityInfrastructureId) {
        this.securityInfrastructureId = securityInfrastructureId;
    }

    @Basic
    @Column(name = "ip_address")
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Basic
    @Column(name = "summary")
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Basic
    @Column(name = "business_case")
    public String getBusinessCase() {
        return businessCase;
    }

    public void setBusinessCase(String businessCase) {
        this.businessCase = businessCase;
    }

    @Basic
    @Column(name = "objectives")
    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    @Basic
    @Column(name = "security_architecture_id")
    public short getSecurityArchitectureId() {
        return securityArchitectureId;
    }

    public void setSecurityArchitectureId(short securityArchitectureId) {
        this.securityArchitectureId = securityArchitectureId;
    }

    @Basic
    @Column(name = "storage_protocol_id")
    public short getStorageProtocolId() {
        return storageProtocolId;
    }

    public void setStorageProtocolId(short storageProtocolId) {
        this.storageProtocolId = storageProtocolId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEntity that = (ProjectEntity) o;
        return securityArchitectureId == that.securityArchitectureId &&
                storageProtocolId == that.storageProtocolId &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(leadUser, that.leadUser) &&
                Objects.equals(technicalLeadUser, that.technicalLeadUser) &&
                Objects.equals(consentModelId, that.consentModelId) &&
                Objects.equals(deidentificationLevel, that.deidentificationLevel) &&
                Objects.equals(projectTypeId, that.projectTypeId) &&
                Objects.equals(securityInfrastructureId, that.securityInfrastructureId) &&
                Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(summary, that.summary) &&
                Objects.equals(businessCase, that.businessCase) &&
                Objects.equals(objectives, that.objectives);
    }

    @Override
    public int hashCode() {

        return Objects.hash(uuid, name, leadUser, technicalLeadUser, consentModelId, deidentificationLevel, projectTypeId, securityInfrastructureId, ipAddress, summary, businessCase, objectives, securityArchitectureId, storageProtocolId);
    }

    public static List<ProjectEntity> getAllProjects() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);
        Root<ProjectEntity> rootEntry = cq.from(ProjectEntity.class);
        CriteriaQuery<ProjectEntity> all = cq.select(rootEntry);
        TypedQuery<ProjectEntity> allQuery = entityManager.createQuery(all);
        List<ProjectEntity> ret =  allQuery.getResultList();

        entityManager.close();

        return ret;
    }

    public static ProjectEntity getProject(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        ProjectEntity ret = entityManager.find(ProjectEntity.class, uuid);

        entityManager.close();

        return ret;
    }

    public static void updateProject(JsonProject project) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        ProjectEntity projectEntity = entityManager.find(ProjectEntity.class, project.getUuid());
        entityManager.getTransaction().begin();
        projectEntity.setName(project.getName());
        projectEntity.setLeadUser(project.getLeadUser());
        projectEntity.setTechnicalLeadUser(project.getTechnicalLeadUser());
        projectEntity.setConsentModelId(project.getConsentModelId());
        projectEntity.setDeidentificationLevel(project.getDeidentificationLevel());
        projectEntity.setProjectTypeId(project.getProjectTypeId());
        projectEntity.setSecurityInfrastructureId(project.getSecurityInfrastructureId());
        projectEntity.setIpAddress(project.getIpAddress());
        projectEntity.setSummary(project.getSummary());
        projectEntity.setBusinessCase(project.getBusinessCase());
        projectEntity.setObjectives(project.getObjectives());
        projectEntity.setSecurityArchitectureId(project.getSecurityArchitectureId());
        projectEntity.setStorageProtocolId(project.getStorageProtocolId());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void saveProject(JsonProject project) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        ProjectEntity projectEntity = new ProjectEntity();
        entityManager.getTransaction().begin();
        projectEntity.setUuid(project.getUuid());
        projectEntity.setName(project.getName());
        projectEntity.setLeadUser(project.getLeadUser());
        projectEntity.setTechnicalLeadUser(project.getTechnicalLeadUser());
        projectEntity.setConsentModelId(project.getConsentModelId());
        projectEntity.setDeidentificationLevel(project.getDeidentificationLevel());
        projectEntity.setProjectTypeId(project.getProjectTypeId());
        projectEntity.setSecurityInfrastructureId(project.getSecurityInfrastructureId());
        projectEntity.setIpAddress(project.getIpAddress());
        projectEntity.setSummary(project.getSummary());
        projectEntity.setBusinessCase(project.getBusinessCase());
        projectEntity.setObjectives(project.getObjectives());
        projectEntity.setSecurityArchitectureId(project.getSecurityArchitectureId());
        projectEntity.setStorageProtocolId(project.getStorageProtocolId());
        entityManager.persist(projectEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteProject(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        ProjectEntity projectEntity = entityManager.find(ProjectEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(projectEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static List<ProjectEntity> search(String expression) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);
        Root<ProjectEntity> rootEntry = cq.from(ProjectEntity.class);

        Predicate predicate = cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%");

        cq.where(predicate);
        TypedQuery<ProjectEntity> query = entityManager.createQuery(cq);
        List<ProjectEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<ProjectEntity> getProjectsFromList(List<String> projects) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);
        Root<ProjectEntity> rootEntry = cq.from(ProjectEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(projects);

        cq.where(predicate);
        TypedQuery<ProjectEntity> query = entityManager.createQuery(cq);

        List<ProjectEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<ProjectEntity> getProjectsForOrganisation(String organisationId) throws Exception {

        EntityManager entityManager = PersistenceManager.getEntityManager();

        Query query = entityManager.createQuery(
                "select p from ProjectEntity p " +
                        "inner join MasterMappingEntity mm on mm.parentUuid = p.uuid and mm.parentMapTypeId = :projectType " +
                        "inner join OrganisationEntity o on o.uuid = mm.childUuid " +
                        "where o.uuid = :orgUuid " +
                        "and mm.childMapTypeId = :subscriberType ");
        query.setParameter("projectType", MapType.PROJECT.getMapType());
        query.setParameter("orgUuid", organisationId);
        query.setParameter("subscriberType", MapType.SUBSCRIBER.getMapType());

        List<ProjectEntity> result = query.getResultList();

        entityManager.close();

        return result;
    }

    public static JsonProject getFullProjectJson(String projectId) throws Exception {
        JsonProject project = new JsonProject(getProject(projectId));

        List<DataSharingAgreementEntity> dsas = getLinkedDsas(projectId);
        List<CohortEntity> basePopulations = getBasePopulations(projectId);
        List<DatasetEntity> dataSets = getDataSets(projectId);
        List<OrganisationEntity> publishers = getLinkedOrganisations(projectId, MapType.PUBLISHER.getMapType());
        List<OrganisationEntity> subscribers = getLinkedOrganisations(projectId, MapType.SUBSCRIBER.getMapType());
        ProjectApplicationPolicyEntity applicationPolicy = ProjectApplicationPolicyEntity.getProjectApplicationPolicyId(projectId);

        if (dsas != null) {
            Map<UUID, String> sharingAgreements = new HashMap<>();

            for (DataSharingAgreementEntity dsa : dsas) {
                sharingAgreements.put(UUID.fromString(dsa.getUuid()), dsa.getName());
            }
            project.setDsas(sharingAgreements);
        }

        if (basePopulations != null) {
            Map<UUID, String> populations = new HashMap<>();

            for (CohortEntity pop : basePopulations) {
                populations.put(UUID.fromString(pop.getUuid()), pop.getName());
            }
            project.setBasePopulation(populations);
        }

        if (dataSets != null) {
            Map<UUID, String> data = new HashMap<>();

            for (DatasetEntity ds : dataSets) {
                data.put(UUID.fromString(ds.getUuid()), ds.getName());
            }
            project.setDataSet(data);
        }

        if (publishers != null) {
            Map<UUID, String> pubs = new HashMap<>();

            for (OrganisationEntity pub : publishers) {
                pubs.put(UUID.fromString(pub.getUuid()), pub.getName());
            }
            project.setPublishers(pubs);
        }

        if (subscribers != null) {
            Map<UUID, String> subs = new HashMap<>();

            for (OrganisationEntity sub : subscribers) {
                subs.put(UUID.fromString(sub.getUuid()), sub.getName());
            }
            project.setSubscribers(subs);
        }

        if (applicationPolicy != null) {
            project.setApplicationPolicy(applicationPolicy.getApplicationPolicyId());
        }

        return project;
    }

    public static List<DataSharingAgreementEntity> getLinkedDsas(String projectId) throws Exception {

        List<String> dsaUuids = MasterMappingEntity.getParentMappings(projectId, MapType.PROJECT.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (dsaUuids.size() > 0)
            ret = DataSharingAgreementEntity.getDSAsFromList(dsaUuids);

        return ret;
    }

    public static List<CohortEntity> getBasePopulations(String projectId) throws Exception {

        List<String> cohortIds = MasterMappingEntity.getChildMappings(projectId, MapType.PROJECT.getMapType(), MapType.COHORT.getMapType());
        List<CohortEntity> ret = new ArrayList<>();

        if (cohortIds.size() > 0)
            ret = CohortEntity.getCohortsFromList(cohortIds);

        return ret;
    }

    public static List<DatasetEntity> getDataSets(String projectId) throws Exception {

        List<String> dataSetIds = MasterMappingEntity.getChildMappings(projectId, MapType.PROJECT.getMapType(), MapType.DATASET.getMapType());
        List<DatasetEntity> ret = new ArrayList<>();

        if (dataSetIds.size() > 0)
            ret = DatasetEntity.getDataSetsFromList(dataSetIds);

        return ret;
    }

    public static List<OrganisationEntity> getLinkedOrganisations(String projectId, Short mapType) throws Exception {

        List<String> orgUUIDs = MasterMappingEntity.getChildMappings(projectId, MapType.PROJECT.getMapType(), mapType);
        List<OrganisationEntity> ret = new ArrayList<>();

        if (orgUUIDs.size() > 0)
            ret = OrganisationEntity.getOrganisationsFromList(orgUUIDs);

        return ret;
    }
}
