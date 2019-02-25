package org.endeavourhealth.datasharingmanagermodel.models.database;

import org.endeavourhealth.datasharingmanagermodel.PersistenceManager;
import org.endeavourhealth.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.datasharingmanagermodel.models.json.*;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Entity
@Table(name = "master_mapping", schema = "data_sharing_manager")
@IdClass(MasterMappingEntityPK.class)
public class MasterMappingEntity {
    private String childUuid;
    private short childMapTypeId;
    private String parentUuid;
    private short parentMapTypeId;
    private byte isDefault;

    @Id
    @Column(name = "child_uuid", nullable = false, length = 36)
    public String getChildUuid() {
        return childUuid;
    }

    public void setChildUuid(String childUuid) {
        this.childUuid = childUuid;
    }

    @Id
    @Column(name = "child_map_type_id", nullable = false)
    public short getChildMapTypeId() {
        return childMapTypeId;
    }

    public void setChildMapTypeId(short childMapTypeId) {
        this.childMapTypeId = childMapTypeId;
    }

    @Id
    @Column(name = "parent_uuid", nullable = false, length = 36)
    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    @Id
    @Column(name = "parent_map_type_id", nullable = false)
    public short getParentMapTypeId() {
        return parentMapTypeId;
    }

    public void setParentMapTypeId(short parentMapTypeId) {
        this.parentMapTypeId = parentMapTypeId;
    }

    @Basic
    @Column(name = "is_default", nullable = false)
    public byte getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(byte isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterMappingEntity that = (MasterMappingEntity) o;

        if (childMapTypeId != that.childMapTypeId) return false;
        if (parentMapTypeId != that.parentMapTypeId) return false;
        if (isDefault != that.isDefault) return false;
        if (childUuid != null ? !childUuid.equals(that.childUuid) : that.childUuid != null) return false;
        if (parentUuid != null ? !parentUuid.equals(that.parentUuid) : that.parentUuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = childUuid != null ? childUuid.hashCode() : 0;
        result = 31 * result + (int) childMapTypeId;
        result = 31 * result + (parentUuid != null ? parentUuid.hashCode() : 0);
        result = 31 * result + (int) parentMapTypeId;
        result = 31 * result + (int) isDefault;
        return result;
    }

    public static void deleteAllMappings(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery(
                "DELETE from MasterMappingEntity m " +
                        "where m.childUuid = :uuid " +
                        "or m.parentUuid = :uuid");
        query.setParameter("uuid", uuid);

        int deletedCount = query.executeUpdate();

        entityManager.getTransaction().commit();

        System.out.println(deletedCount + " deleted");
        entityManager.close();
    }

    public static void saveParentMappings(Map<UUID, String> parents, Short parentMapTypeId, String childUuid, Short childMapTypeId) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        parents.forEach((k, v) -> {
            MasterMappingEntity mme = new MasterMappingEntity();
            entityManager.getTransaction().begin();
            mme.setChildUuid(childUuid);
            mme.setChildMapTypeId(childMapTypeId);
            mme.setParentUuid(k.toString());
            mme.setParentMapTypeId(parentMapTypeId);
            entityManager.persist(mme);
            entityManager.getTransaction().commit();
        });

        entityManager.close();

    }

    public static void saveChildMappings(Map<UUID, String> children, Short childMapTypeId, String parentUuid, Short parentMapTypeId) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        children.forEach((k, v) -> {
            MasterMappingEntity mme = new MasterMappingEntity();
            entityManager.getTransaction().begin();
            mme.setChildUuid(k.toString());
            mme.setChildMapTypeId(childMapTypeId);
            mme.setParentUuid(parentUuid);
            mme.setParentMapTypeId(parentMapTypeId);
            entityManager.persist(mme);
            entityManager.getTransaction().commit();
        });

        entityManager.close();
    }

    public static List<String> getParentMappings(String childUuid, Short childMapTypeId, Short parentMapTypeId) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MasterMappingEntity> cq = cb.createQuery(MasterMappingEntity.class);
        Root<MasterMappingEntity> rootEntry = cq.from(MasterMappingEntity.class);

        Predicate predicate = cb.and(cb.equal(rootEntry.get("childUuid"), childUuid ),
                cb.equal(rootEntry.get("childMapTypeId"), childMapTypeId),
                cb.equal(rootEntry.get("parentMapTypeId"), parentMapTypeId));

        cq.where(predicate);
        TypedQuery<MasterMappingEntity> query = entityManager.createQuery(cq);
        List<MasterMappingEntity> maps =  query.getResultList();

        List<String> parents = new ArrayList<>();
        for(MasterMappingEntity mme : maps){
            parents.add(mme.getParentUuid());
        }

        entityManager.close();

        return parents;
    }

    public static List<String> getChildMappings(String parentUuid, Short parentMapTypeId, Short childMapTypeId) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MasterMappingEntity> cq = cb.createQuery(MasterMappingEntity.class);
        Root<MasterMappingEntity> rootEntry = cq.from(MasterMappingEntity.class);

        Predicate predicate = cb.and(cb.equal(rootEntry.get("parentUuid"), parentUuid ),
                cb.equal(rootEntry.get("parentMapTypeId"), parentMapTypeId),
                cb.equal(rootEntry.get("childMapTypeId"), childMapTypeId));

        cq.where(predicate);
        TypedQuery<MasterMappingEntity> query = entityManager.createQuery(cq);
        List<MasterMappingEntity> maps =  query.getResultList();

        List<String> children = new ArrayList<>();
        for(MasterMappingEntity mme : maps){
            children.add(mme.getChildUuid());
        }

        entityManager.close();

        return children;
    }

    public static List<String> getParentMappingsFromList(List<String> childUuids, Short childMapTypeId, Short parentMapTypeId) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MasterMappingEntity> cq = cb.createQuery(MasterMappingEntity.class);
        Root<MasterMappingEntity> rootEntry = cq.from(MasterMappingEntity.class);

        Predicate predicate = cb.and((rootEntry.get("childUuid").in(childUuids) ),
                cb.equal(rootEntry.get("childMapTypeId"), childMapTypeId),
                cb.equal(rootEntry.get("parentMapTypeId"), parentMapTypeId));

        cq.where(predicate);
        TypedQuery<MasterMappingEntity> query = entityManager.createQuery(cq);
        List<MasterMappingEntity> maps =  query.getResultList();

        List<String> children = new ArrayList<>();
        for(MasterMappingEntity mme : maps){
            children.add(mme.getParentUuid());
        }

        entityManager.close();

        return children;
    }

    public static void saveOrganisationMappings(JsonOrganisation organisation) throws Exception {
        //Default to organisation
        Short organisationType = MapType.ORGANISATION.getMapType();

        if (organisation.getIsService().equals("0")) {
            if (organisation.getRegions() != null) {
                Map<UUID, String> regions = organisation.getRegions();
                saveParentMappings(regions, MapType.REGION.getMapType(), organisation.getUuid(), MapType.ORGANISATION.getMapType());
            }

            if (organisation.getChildOrganisations() != null) {
                Map<UUID, String> childOrganisations = organisation.getChildOrganisations();
                saveChildMappings(childOrganisations, MapType.ORGANISATION.getMapType(), organisation.getUuid(), MapType.ORGANISATION.getMapType());

                CompletableFuture.runAsync(() -> {
                    try {
                        AddressEntity.getGeoLocationsForOrganisations(new ArrayList<>(childOrganisations.keySet()));
                    } catch (Exception e) {
                        // ignore error;
                    }
                });
            }

            if (organisation.getServices() != null) {
                Map<UUID, String> services = organisation.getServices();
                saveChildMappings(services, MapType.SERVICE.getMapType(), organisation.getUuid(), MapType.ORGANISATION.getMapType());

                CompletableFuture.runAsync(() -> {
                    try {
                        AddressEntity.getGeoLocationsForOrganisations(new ArrayList<>(services.keySet()));
                    } catch (Exception e) {
                        // ignore error;
                    }
                });
            }

            if (organisation.getDpaPublishing() != null) {
                Map<UUID, String> dpas = organisation.getDpaPublishing();
                saveParentMappings(dpas, MapType.DATAPROCESSINGAGREEMENT.getMapType(), organisation.getUuid(), MapType.PUBLISHER.getMapType());
            }

            if (organisation.getDsaPublishing() != null) {
                Map<UUID, String> dsas = organisation.getDsaPublishing();
                saveParentMappings(dsas, MapType.DATASHARINGAGREEMENT.getMapType(), organisation.getUuid(), MapType.PUBLISHER.getMapType());
            }

            if (organisation.getDsaSubscribing() != null) {
                Map<UUID, String> dsaSub = organisation.getDsaSubscribing();
                saveParentMappings(dsaSub, MapType.DATASHARINGAGREEMENT.getMapType(), organisation.getUuid(), MapType.SUBSCRIBER.getMapType());
            }
        } else {
            organisationType = MapType.SERVICE.getMapType();
        }

        if (organisation.getParentOrganisations() != null) {
            Map<UUID, String> parentOrganisations = organisation.getParentOrganisations();
            saveParentMappings(parentOrganisations, MapType.ORGANISATION.getMapType(), organisation.getUuid(), organisationType);
        }
    }

    public static void saveRegionMappings(JsonRegion region) throws Exception {

        if (region.getParentRegions() != null) {
            Map<UUID, String> parentRegions = region.getParentRegions();
            saveParentMappings(parentRegions, MapType.REGION.getMapType(), region.getUuid(), MapType.REGION.getMapType());
        }

        if (region.getChildRegions() != null) {
            Map<UUID, String> childRegions = region.getChildRegions();
            saveChildMappings(childRegions, MapType.REGION.getMapType(), region.getUuid(), MapType.REGION.getMapType());
        }

        if (region.getOrganisations() != null) {
            Map<UUID, String> organisations = region.getOrganisations();
            saveChildMappings(organisations, MapType.ORGANISATION.getMapType(), region.getUuid(), MapType.REGION.getMapType());

            CompletableFuture.runAsync(() -> {
                try {
                    AddressEntity.getGeoLocationsForOrganisations(new ArrayList<>(organisations.keySet()));
                } catch (Exception e) {
                    // ignore error;
                }
            });
        }

        if (region.getSharingAgreements() != null) {
            Map<UUID, String> sharingAgreements = region.getSharingAgreements();
            saveChildMappings(sharingAgreements, MapType.DATASHARINGAGREEMENT.getMapType(), region.getUuid(), MapType.REGION.getMapType());
        }

        if (region.getProcessingAgreements() != null) {
            Map<UUID, String> processingAgreements = region.getProcessingAgreements();
            saveChildMappings(processingAgreements, MapType.DATAPROCESSINGAGREEMENT.getMapType(), region.getUuid(), MapType.REGION.getMapType());
        }
    }

    public static void saveDataSharingAgreementMappings(JsonDSA dsa) throws Exception {

        if (dsa.getDataFlows() != null) {
            Map<UUID, String> dataFlows = dsa.getDataFlows();
            saveChildMappings(dataFlows, MapType.DATAFLOW.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }

        if (dsa.getRegions() != null) {
            Map<UUID, String> regions = dsa.getRegions();
            saveParentMappings(regions, MapType.REGION.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }

        if (dsa.getPublishers() != null) {
            Map<UUID, String> publishers = dsa.getPublishers();
            saveChildMappings(publishers, MapType.PUBLISHER.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());

            CompletableFuture.runAsync(() -> {
                try {
                    AddressEntity.getGeoLocationsForOrganisations(new ArrayList<>(publishers.keySet()));
                } catch (Exception e) {
                    // ignore error;
                }
            });
        }

        if (dsa.getSubscribers() != null) {
            Map<UUID, String> subscribers = dsa.getSubscribers();
            saveChildMappings(subscribers, MapType.SUBSCRIBER.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());

            CompletableFuture.runAsync(() -> {
                try {
                    AddressEntity.getGeoLocationsForOrganisations(new ArrayList<>(subscribers.keySet()));
                } catch (Exception e) {
                    // ignore error;
                }
            });
        }

        if (dsa.getDocumentations() != null) {
            Map<UUID, String> documentation = new HashMap<>();
            List<JsonDocumentation> jsonDocumentations =  dsa.getDocumentations();
            for (JsonDocumentation doc : jsonDocumentations) {
                documentation.put(UUID.fromString(doc.getUuid()), doc.getTitle());
            }
            saveChildMappings(documentation, MapType.DOCUMENT.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }

        if (dsa.getPurposes() != null) {
            Map<UUID, String> purposes = new HashMap<>();
            List<JsonPurpose> jsonPurposes =  dsa.getPurposes();
            for (JsonPurpose purp : jsonPurposes) {
                purposes.put(UUID.fromString(purp.getUuid()), purp.getTitle());
            }
            saveChildMappings(purposes, MapType.PURPOSE.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }

        if (dsa.getBenefits() != null) {
            Map<UUID, String> benefits = new HashMap<>();
            List<JsonPurpose> jsonBenefits =  dsa.getBenefits();
            for (JsonPurpose benef : jsonBenefits) {
                benefits.put(UUID.fromString(benef.getUuid()), benef.getTitle());
            }
            saveChildMappings(benefits, MapType.BENEFIT.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }
    }

    public static void saveDataFlowMappings(JsonDataFlow dataFlow) throws Exception {

        if (dataFlow.getDsas() != null) {
            Map<UUID, String> dsas = dataFlow.getDsas();
            saveParentMappings(dsas, MapType.DATASHARINGAGREEMENT.getMapType(),  dataFlow.getUuid(), MapType.DATAFLOW.getMapType());
        }

        if (dataFlow.getDpas() != null) {
            Map<UUID, String> dpas = dataFlow.getDpas();
            saveParentMappings(dpas, MapType.DATAPROCESSINGAGREEMENT.getMapType(), dataFlow.getUuid(), MapType.DATAFLOW.getMapType());
        }

        if (dataFlow.getExchanges() != null) {
            Map<UUID, String> exchanges = dataFlow.getExchanges();
            saveChildMappings(exchanges, MapType.DATAEXCHANGE.getMapType(), dataFlow.getUuid(), MapType.DATAFLOW.getMapType());
        }

        if (dataFlow.getPublishers() != null) {
            Map<UUID, String> publishers = dataFlow.getPublishers();
            saveChildMappings(publishers, MapType.PUBLISHER.getMapType(), dataFlow.getUuid(), MapType.DATAFLOW.getMapType());
        }

        if (dataFlow.getSubscribers() != null) {
            Map<UUID, String> subscribers = dataFlow.getSubscribers();
            saveChildMappings(subscribers, MapType.SUBSCRIBER.getMapType(), dataFlow.getUuid(), MapType.DATAFLOW.getMapType());
        }

        if (dataFlow.getDocumentations() != null) {
            Map<UUID, String> documentation = new HashMap<>();
            List<JsonDocumentation> jsonDocumentations =  dataFlow.getDocumentations();
            for (JsonDocumentation doc : jsonDocumentations) {
                documentation.put(UUID.fromString(doc.getUuid()), doc.getTitle());
            }
            saveChildMappings(documentation, MapType.DOCUMENT.getMapType(), dataFlow.getUuid(), MapType.DATAFLOW.getMapType());
        }
    }

    public static void saveProjectMappings(JsonProject project) throws Exception {

        if (project.getPublishers() != null) {
            Map<UUID, String> publishers = project.getPublishers();
            saveChildMappings(publishers, MapType.PUBLISHER.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }

        if (project.getSubscribers() != null) {
            Map<UUID, String> subscribers = project.getSubscribers();
            saveChildMappings(subscribers, MapType.SUBSCRIBER.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }

        if (project.getBasePopulation() != null) {
            Map<UUID, String> dsas = project.getBasePopulation();
            saveChildMappings(dsas, MapType.COHORT.getMapType(),  project.getUuid(), MapType.PROJECT.getMapType());
        }

        if (project.getDataSet() != null) {
            Map<UUID, String> dpas = project.getDataSet();
            saveChildMappings(dpas, MapType.DATASET.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }

        if (project.getDsas() != null) {
            Map<UUID, String> exchanges = project.getDsas();
            saveParentMappings(exchanges, MapType.DATASHARINGAGREEMENT.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }
    }

    public static void saveDataExchangeMappings(JsonDataExchange dataExchange) throws Exception {

        if (dataExchange.getDataFlows() != null) {
            Map<UUID, String> dataFlows = dataExchange.getDataFlows();
            saveParentMappings(dataFlows, MapType.DATAFLOW.getMapType(),  dataExchange.getUuid(), MapType.DATAEXCHANGE.getMapType());
        }
    }

    public static void saveDataProcessingAgreementMappings(JsonDPA dpa) throws Exception {

        if (dpa.getDataFlows() != null) {
            Map<UUID, String> dataFlows = dpa.getDataFlows();
            saveChildMappings(dataFlows, MapType.DATAFLOW.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        }

        if (dpa.getCohorts() != null) {
            Map<UUID, String> cohorts = dpa.getCohorts();
            saveChildMappings(cohorts, MapType.COHORT.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        }

        if (dpa.getRegions() != null) {
            Map<UUID, String> regions = dpa.getRegions();
            saveParentMappings(regions, MapType.REGION.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        }

        if (dpa.getDataSets() != null) {
            Map<UUID, String> datasets = dpa.getDataSets();
            saveChildMappings(datasets, MapType.DATASET.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        }

        if (dpa.getDocumentations() != null) {
            Map<UUID, String> documentation = new HashMap<>();
            List<JsonDocumentation> jsonDocumentations =  dpa.getDocumentations();
            for (JsonDocumentation doc : jsonDocumentations) {
                documentation.put(UUID.fromString(doc.getUuid()), doc.getTitle());
            }
            saveChildMappings(documentation, MapType.DOCUMENT.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        }

        if (dpa.getPublishers() != null) {
            Map<UUID, String> publishers = dpa.getPublishers();
            saveChildMappings(publishers, MapType.PUBLISHER.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());

            CompletableFuture.runAsync(() -> {
                try {
                    AddressEntity.getGeoLocationsForOrganisations(new ArrayList<>(publishers.keySet()));
                } catch (Exception e) {
                    // ignore error;
                }
            });
        }

        if (dpa.getPurposes() != null) {
            Map<UUID, String> purposes = new HashMap<>();
            List<JsonPurpose> jsonPurposes =  dpa.getPurposes();
            for (JsonPurpose purp : jsonPurposes) {
                purposes.put(UUID.fromString(purp.getUuid()), purp.getTitle());
            }
            saveChildMappings(purposes, MapType.PURPOSE.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        }

        if (dpa.getBenefits() != null) {
            Map<UUID, String> benefits = new HashMap<>();
            List<JsonPurpose> jsonBenefits =  dpa.getBenefits();
            for (JsonPurpose benef : jsonBenefits) {
                benefits.put(UUID.fromString(benef.getUuid()), benef.getTitle());
            }
            saveChildMappings(benefits, MapType.BENEFIT.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        }
    }

    public static void saveCohortMappings(JsonCohort cohort) throws Exception {

        if (cohort.getDpas() != null) {
            Map<UUID, String> dataDpas = cohort.getDpas();
            saveParentMappings(dataDpas, MapType.DATAPROCESSINGAGREEMENT.getMapType(), cohort.getUuid(), MapType.COHORT.getMapType());
        }
    }

    public static void saveDataSetMappings(JsonDataSet dataset) throws Exception {

        if (dataset.getDpas() != null) {
            Map<UUID, String> dataDpas = dataset.getDpas();
            saveParentMappings(dataDpas, MapType.DATAPROCESSINGAGREEMENT.getMapType(), dataset.getUuid(), MapType.DATASET.getMapType());
        }
    }

    public static void bulkSaveMappings(List<MasterMappingEntity> mappings) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();
        int batchSize = 50;
        entityManager.getTransaction().begin();

        for(int i = 0; i < mappings.size(); ++i) {
            MasterMappingEntity mapping = mappings.get(i);
            entityManager.merge(mapping);
            if(i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
