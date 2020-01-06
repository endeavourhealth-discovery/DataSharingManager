package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectScheduleDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.*;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.uiaudit.logic.AuditCompareLogic;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.xml.crypto.KeySelector;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MasterMappingDAL {

    // This method should be removed, and calls redirected to updateMappings (to be written)
    public void deleteAllMappings(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            entityManager.getTransaction().begin();
            Query query = entityManager.createQuery(
                    "DELETE from MasterMappingEntity m " +
                            "where m.childUuid = :uuid " +
                            "or m.parentUuid = :uuid");
            query.setParameter("uuid", uuid);

            int deletedCount = query.executeUpdate();

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    // This method should be removed, and calls redirected to updateMappings
    public void saveParentMappings(Map<UUID, String> parents, Short parentMapTypeId, String childUuid, Short childMapTypeId) throws Exception {
        // Convert to List
        List<String> parentUuids = new ArrayList<String>();
        parents.forEach((k, v) -> parentUuids.add(k.toString()));

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        try {
            entityManager.getTransaction().begin();
            saveMappings(true, childUuid, parentUuids, childMapTypeId, parentMapTypeId, entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public JsonNode updateDataSetMappings(JsonDataSet updatedDataSet, DatasetEntity oldDataset, JsonNode auditJson, EntityManager entityManager) throws Exception {

        String uuid = (updatedDataSet != null? updatedDataSet.getUuid(): oldDataset.getUuid());

        // DPAs
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldDataset == null ? null : oldDataset.getDpas()),
                (updatedDataSet == null ? null : updatedDataSet.getDpas()),
                MapType.DATASET.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson, entityManager);

        return auditJson;
    }

    public JsonNode updateCohortMappings(JsonCohort updatedCohort, CohortEntity oldCohort, JsonNode auditJson, EntityManager entityManager) throws Exception {

        String uuid = (updatedCohort != null ? updatedCohort.getUuid() : oldCohort.getUuid());

        // DPAs
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldCohort == null ? null : oldCohort.getDpas()),
                (updatedCohort == null ? null : updatedCohort.getDpas()), MapType.COHORT.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson, entityManager);

        return auditJson;

    }

    public JsonNode updateDataProcessingAgreementMappings(JsonDPA updatedDPA, DataProcessingAgreementEntity oldDPA, JsonNode auditJson, EntityManager entityManager) throws Exception {

        String uuid = (updatedDPA != null ? updatedDPA.getUuid() : oldDPA.getUuid());

        // Purposes
        auditJson = updatePurposesAndGetAudit(false, uuid, (oldDPA == null ? null : oldDPA.getPurposes()),
                (updatedDPA == null ? null : updatedDPA.getPurposes()), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PURPOSE.getMapType(), auditJson, entityManager);

        // Benefits
        auditJson = updatePurposesAndGetAudit(false, uuid, (oldDPA == null ? null : oldDPA.getBenefits()),
                (updatedDPA == null ? null : updatedDPA.getBenefits()), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.BENEFIT.getMapType(), auditJson, entityManager);

        // Regions
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldDPA == null ? null : oldDPA.getRegions()),
                (updatedDPA == null ? null : updatedDPA.getRegions()), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.REGION.getMapType(), auditJson, entityManager);

        // Publishers
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldDPA == null ? null : oldDPA.getPublishers()),
                (updatedDPA == null ? null : updatedDPA.getPublishers()), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType(), auditJson, entityManager);

        // Documentation
        auditJson = updateMappingsAndGetAuditForObjList(false, uuid, (oldDPA == null ? null : oldDPA.getDocumentations()),
                (updatedDPA == null ? null : updatedDPA.getDocumentations()), MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.DOCUMENT.getMapType(), auditJson, entityManager);


        return auditJson;
    }


    public JsonNode updateDataSharingAgreementMappings(JsonDSA updatedDSA, DataSharingAgreementEntity oldDSA, JsonNode auditJson, EntityManager entityManager) throws Exception {

        String uuid = (updatedDSA != null ? updatedDSA.getUuid() : oldDSA.getUuid());

        // Purposes
        auditJson = updatePurposesAndGetAudit(false, uuid, (oldDSA == null ? null : oldDSA.getPurposes()),
                (updatedDSA == null ? null : updatedDSA.getPurposes()), MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PURPOSE.getMapType(), auditJson, entityManager);

        // Benefits
        auditJson = updatePurposesAndGetAudit(false, uuid, (oldDSA == null ? null : oldDSA.getBenefits()),
                (updatedDSA == null ? null : updatedDSA.getBenefits()), MapType.DATASHARINGAGREEMENT.getMapType(), MapType.BENEFIT.getMapType(), auditJson, entityManager);

        // Regions
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldDSA == null ? null : oldDSA.getRegions()),
                (updatedDSA == null ? null : updatedDSA.getRegions()), MapType.DATASHARINGAGREEMENT.getMapType(), MapType.REGION.getMapType(), auditJson, entityManager);

        // Projects
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldDSA == null ? null : oldDSA.getProjects()),
                (updatedDSA == null ? null : updatedDSA.getProjects()), MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PROJECT.getMapType(), auditJson, entityManager);

        // Publishers
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldDSA == null ? null : oldDSA.getPublishers()),
                (updatedDSA == null ? null : updatedDSA.getPublishers()), MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType(), auditJson, entityManager);

        // Subscribers
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldDSA == null ? null : oldDSA.getSubscribers()),
                (updatedDSA == null ? null : updatedDSA.getSubscribers()), MapType.DATASHARINGAGREEMENT.getMapType(), MapType.SUBSCRIBER.getMapType(), auditJson, entityManager);

        // Documentation
        auditJson = updateMappingsAndGetAuditForObjList(false, uuid, (oldDSA == null ? null : oldDSA.getDocumentations()),
                (updatedDSA == null ? null : updatedDSA.getDocumentations()), MapType.DATASHARINGAGREEMENT.getMapType(), MapType.DOCUMENT.getMapType(), auditJson, entityManager);


        return auditJson;
    }

    public JsonNode updateRegionMappings(JsonRegion updatedRegion, RegionEntity oldRegion, JsonNode auditJson, EntityManager entityManager) throws Exception {
        String uuid = (updatedRegion != null ? updatedRegion.getUuid() : oldRegion.getUuid());

        // DSAs
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldRegion == null ? null : oldRegion.getSharingAgreements()),
                (updatedRegion == null ? null : updatedRegion.getSharingAgreements()), MapType.REGION.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType(), auditJson, entityManager);

        // DPAs
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldRegion == null ? null : oldRegion.getProcessingAgreements()),
                (updatedRegion == null ? null : updatedRegion.getProcessingAgreements()), MapType.REGION.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson, entityManager);

        // Parent Regions
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldRegion == null ? null : oldRegion.getParentRegions()),
                (updatedRegion == null ? null : updatedRegion.getParentRegions()), MapType.REGION.getMapType(), MapType.REGION.getMapType(), auditJson, entityManager);

        // Child Regions
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldRegion == null ? null : oldRegion.getChildRegions()),
                (updatedRegion == null ? null : updatedRegion.getChildRegions()), MapType.REGION.getMapType(), MapType.REGION.getMapType(), auditJson, entityManager);

        // Organisations
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldRegion == null ? null : oldRegion.getOrganisations()),
                (updatedRegion == null ? null : updatedRegion.getOrganisations()), MapType.REGION.getMapType(), MapType.ORGANISATION.getMapType(), auditJson, entityManager);

        return auditJson;
    }

    public JsonNode updateOrganisationMappings(JsonOrganisation updatedOrganisation, OrganisationEntity oldOrganisation, JsonNode auditJson, EntityManager entityManager) throws Exception {
        String uuid = (updatedOrganisation != null ? updatedOrganisation.getUuid() : oldOrganisation.getUuid());

        // Regions
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getRegions()),
                (updatedOrganisation == null ? null : updatedOrganisation.getRegions()), MapType.ORGANISATION.getMapType(), MapType.REGION.getMapType(), auditJson, entityManager);

        // Parent Organisations
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getParentOrganisations()),
                (updatedOrganisation == null ? null : updatedOrganisation.getParentOrganisations()), MapType.ORGANISATION.getMapType(), MapType.ORGANISATION.getMapType(), auditJson, entityManager);

        // Child Organisations
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldOrganisation == null ? null : oldOrganisation.getChildOrganisations()),
                (updatedOrganisation == null ? null : updatedOrganisation.getChildOrganisations()), MapType.ORGANISATION.getMapType(), MapType.ORGANISATION.getMapType(), auditJson, entityManager);

        // Services
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldOrganisation == null ? null : oldOrganisation.getServices()),
                (updatedOrganisation == null ? null : updatedOrganisation.getServices()), MapType.ORGANISATION.getMapType(), MapType.SERVICE.getMapType(), auditJson, entityManager);

        // Publishing DPA
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getDpaPublishing()),
                (updatedOrganisation == null ? null : updatedOrganisation.getDpaPublishing()), MapType.PUBLISHER.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson, entityManager);

        // Publishing DSA
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getDsaPublishing()),
                (updatedOrganisation == null ? null : updatedOrganisation.getDsaPublishing()), MapType.PUBLISHER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType(), auditJson, entityManager);

        // Subscribing DSA
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getDsaSubscribing()),
                (updatedOrganisation == null ? null : updatedOrganisation.getDsaSubscribing()), MapType.SUBSCRIBER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType(), auditJson, entityManager);

        return auditJson;
    }

    private JsonNode appendToJson(String changeDescription, List<String> mappings, String type, JsonNode auditJson) throws Exception {
        if (!mappings.isEmpty()) {
            return new AuditCompareLogic().generateListDifferenceAuditJson(auditJson, changeDescription, mappings, type);
        }

        return auditJson;
    }

    private void updateMappings(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                List<String> updatedMappings, Short thisMapTypeId, Short otherMapTypeId,
                                List<String> removedMappings, List<String> addedMappings, EntityManager entityManager) throws Exception {

        if (oldMappings != null) {
            for (String oldMapping : oldMappings) {
                if (updatedMappings == null || !updatedMappings.contains(oldMapping)) {
                    removedMappings.add(oldMapping);
                }
            }
        }

        if (updatedMappings != null) {
            for (String updatedMapping : updatedMappings) {
                if (oldMappings == null || !oldMappings.contains(updatedMapping)) {
                    addedMappings.add(updatedMapping);
                }
            }
        }

        if (!removedMappings.isEmpty() || !addedMappings.isEmpty()) {
            try {

                if (!removedMappings.isEmpty()) {
                    deleteMappings(thisItemIsChild, thisItem, removedMappings, thisMapTypeId, otherMapTypeId, entityManager);
                }

                if (!addedMappings.isEmpty()) {
                    saveMappings(thisItemIsChild, thisItem, addedMappings, thisMapTypeId, otherMapTypeId, entityManager);
                }

            } catch (Exception e) {
                throw e;
            }
        }
    }

    private void saveMappings(boolean thisItemIsChild, String thisItem, List<String> mappingsToAdd, Short thisMapTypeId, Short otherMapTypeId, EntityManager entityManager) throws Exception {
        mappingsToAdd.forEach((mapping) -> {
            MasterMappingEntity mme;
            if (thisItemIsChild) {
                mme = new MasterMappingEntity(thisItem, thisMapTypeId, mapping, otherMapTypeId);
            } else {
                mme = new MasterMappingEntity(mapping, otherMapTypeId, thisItem, thisMapTypeId);
            }
            entityManager.persist(mme);
        });
    }


    private void deleteMappings(boolean thisItemIsChild, String thisItem, List<String> mappingsToDelete, Short thisMapTypeId, Short otherMapTypeId, EntityManager entityManager) throws Exception {
        mappingsToDelete.forEach((mapping) -> {
            MasterMappingEntity mme;
            if (thisItemIsChild) {
                mme = new MasterMappingEntity(thisItem, thisMapTypeId, mapping, otherMapTypeId);
            } else {
                mme = new MasterMappingEntity(mapping, otherMapTypeId, thisItem, thisMapTypeId);
            }
            entityManager.remove(entityManager.merge(mme));
        });
    }

    // Should become redundant
    private void saveChildMappings(Map<UUID, String> children, Short childMapTypeId, String parentUuid, Short parentMapTypeId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
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
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public List<String> getParentMappingsFromList(List<String> childUuids, Short childMapTypeId, Short parentMapTypeId) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<MasterMappingEntity> cq = cb.createQuery(MasterMappingEntity.class);
            Root<MasterMappingEntity> rootEntry = cq.from(MasterMappingEntity.class);

            Predicate predicate = cb.and((rootEntry.get("childUuid").in(childUuids)),
                    cb.equal(rootEntry.get("childMapTypeId"), childMapTypeId),
                    cb.equal(rootEntry.get("parentMapTypeId"), parentMapTypeId));

            cq.where(predicate);
            TypedQuery<MasterMappingEntity> query = entityManager.createQuery(cq);
            List<MasterMappingEntity> maps = query.getResultList();

            List<String> children = new ArrayList<>();
            for (MasterMappingEntity mme : maps) {
                children.add(mme.getParentUuid());
            }

            return children;
        } finally {
            entityManager.close();
        }

    }

    public void saveOrganisationMappings(JsonOrganisation organisation) throws Exception {
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
                        new AddressDAL().getGeoLocationsForOrganisations(new ArrayList<>(childOrganisations.keySet()));
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
                        new AddressDAL().getGeoLocationsForOrganisations(new ArrayList<>(services.keySet()));
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

    public JsonNode updateProjectMappings(JsonProject updatedProject, ProjectEntity oldProject, JsonNode auditJson, EntityManager entityManager) throws Exception {
        // Publishers
        auditJson = updateMappingsAndGetAudit(false, updatedProject.getUuid(), oldProject.getPublishers(),
                updatedProject.getPublishers(), MapType.PROJECT.getMapType(), MapType.PUBLISHER.getMapType(), auditJson, entityManager);

        // Subscriber
        auditJson = updateMappingsAndGetAudit(false, updatedProject.getUuid(), oldProject.getSubscribers(),
                updatedProject.getSubscribers(), MapType.PROJECT.getMapType(), MapType.SUBSCRIBER.getMapType(), auditJson, entityManager);

        // Cohorts
        auditJson = updateMappingsAndGetAudit(false, updatedProject.getUuid(), oldProject.getCohorts(),
                updatedProject.getCohorts(), MapType.PROJECT.getMapType(), MapType.COHORT.getMapType(), auditJson, entityManager);

        // DataSets
        auditJson = updateMappingsAndGetAudit(false, updatedProject.getUuid(), oldProject.getDataSets(),
                updatedProject.getDataSets(), MapType.PROJECT.getMapType(), MapType.DATASET.getMapType(), auditJson, entityManager);

        // DSA
        auditJson = updateMappingsAndGetAudit(true, updatedProject.getUuid(), oldProject.getDsas(),
                updatedProject.getDsas(), MapType.PROJECT.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType(), auditJson, entityManager);

        // Documents
        auditJson = updateMappingsAndGetAuditForDocuments(updatedProject.getUuid(), oldProject.getDocumentations(),
                updatedProject.getDocumentations(), MapType.PROJECT.getMapType(), auditJson, entityManager);

        // Extract Technical Details
        auditJson = updateMappingsAndGetAuditForExtractTechnicalDetails(updatedProject.getUuid(), oldProject.getExtractTechnicalDetails(),
                updatedProject.getExtractTechnicalDetails(), MapType.PROJECT.getMapType(), auditJson, entityManager);

        //Schedules
        auditJson = updateMappingsAndGetAuditForSchedule(updatedProject.getUuid(), oldProject.getSchedule(),
                updatedProject.getSchedule(), MapType.PROJECT.getMapType(), auditJson, entityManager);

        return auditJson;
    }

    private <T extends JsonItem> JsonNode updateMappingsAndGetAuditForObjList(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                               List<T> updatedObjectList, Short thisMapTypeId, Short otherMapTypeId,
                                               JsonNode auditJson, EntityManager entityManager) throws Exception {
        List<String> updatedMappings = new ArrayList<String>();
        if (updatedObjectList != null) {
            updatedObjectList.forEach((o) -> updatedMappings.add(o.getUuid()));
        }

        return updateMappingsAndGetAudit(thisItemIsChild, thisItem, oldMappings, updatedMappings, thisMapTypeId, otherMapTypeId, auditJson, entityManager);
    }

    private JsonNode updateMappingsAndGetAudit(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                               Map<UUID, String> updatedMap, Short thisMapTypeId, Short otherMapTypeId,
                                               JsonNode auditJson, EntityManager entityManager) throws Exception {
        List<String> updatedMappings = new ArrayList<String>();
        if (updatedMap != null) {
            updatedMap.forEach((k, v) -> updatedMappings.add(k.toString()));
        }

        return updateMappingsAndGetAudit(thisItemIsChild, thisItem, oldMappings, updatedMappings, thisMapTypeId, otherMapTypeId, auditJson, entityManager);
    }

    private JsonNode updateMappingsAndGetAudit(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                               List<String> updatedMappings, Short thisMapTypeId, Short otherMapTypeId,
                                               JsonNode auditJson, EntityManager entityManager) throws Exception {

        List<String> removedMappings = new ArrayList<>();
        List<String> addedMappings = new ArrayList<>();

        updateMappings(thisItemIsChild, thisItem, oldMappings, updatedMappings,
                thisMapTypeId, otherMapTypeId, removedMappings, addedMappings, entityManager);

        auditJson = appendToJson(getChangeDescription(thisItemIsChild, false, thisMapTypeId, otherMapTypeId),
                removedMappings, MapType.valueOfTypeId(otherMapTypeId), auditJson);
        auditJson = appendToJson(getChangeDescription(thisItemIsChild, true, thisMapTypeId, otherMapTypeId),
                addedMappings, MapType.valueOfTypeId(otherMapTypeId), auditJson);

        return auditJson;
    }

    private JsonNode updatePurposesAndGetAudit(boolean thisItemIsChild, String thisItem, List<PurposeEntity> oldPurposes,
                                               List<JsonPurpose> updatedPurposes, Short thisMapTypeId, Short otherMapTypeId,
                                               JsonNode auditJson, EntityManager entityManager) throws Exception {

        // First, identify what has changed
        List<JsonPurpose> addedPurposes = new ArrayList<>();
        List<PurposeEntity> removedPurposes = new ArrayList<>();
        List<JsonPurpose> changedPurposes = new ArrayList<>();

        if (oldPurposes != null) {
            for (PurposeEntity oldPurpose : oldPurposes) {
                if (updatedPurposes == null || updatedPurposes.stream().noneMatch(up -> up.getUuid().equals(oldPurpose.getUuid()))) {
                    removedPurposes.add(oldPurpose);
                }
            }
        }

        if (updatedPurposes != null) {
            for (JsonPurpose updatedPurpose : updatedPurposes) {
                if (oldPurposes == null) {
                    addedPurposes.add(updatedPurpose);
                } else {
                    Optional<PurposeEntity> oldPurpose = oldPurposes.stream().filter(op -> op.getUuid().equals(updatedPurpose.getUuid())).findFirst();
                    if (oldPurpose.isPresent()) {
                        if (!updatedPurpose.toString().equals(oldPurpose.get().toString())) {
                            changedPurposes.add(updatedPurpose);
                        } //else: Unchanged - no action required.
                    } else {
                        addedPurposes.add(updatedPurpose);
                    }
                }
            }
        }

        List<String> removalLog = new ArrayList<>();
        List<String> additionLog = new ArrayList<>();

        // Now apply changes
        if (!removedPurposes.isEmpty()) {
            List<String> removedPurposeUuids = removedPurposes.stream().map(PurposeEntity::getUuid).collect(Collectors.toList());
            deleteMappings(thisItemIsChild, thisItem, removedPurposeUuids, thisMapTypeId, otherMapTypeId, entityManager);
            removedPurposes.forEach(rp -> removalLog.add(rp.toString()));
        }

        if (!addedPurposes.isEmpty()) {
            List<String> addedPurposeUuids = addedPurposes.stream().map(JsonPurpose::getUuid).collect(Collectors.toList());
            saveMappings(thisItemIsChild, thisItem, addedPurposeUuids, thisMapTypeId, otherMapTypeId, entityManager);
            addedPurposes.forEach(ap -> additionLog.add(ap.toString()));
        }

        for (JsonPurpose p : changedPurposes) {
            Optional<PurposeEntity> oldPe = oldPurposes.stream().filter(pe -> pe.getUuid().equals(p.getUuid())).findAny();
            // For now, log as removal + addition.  Uuid will be unchanged.
            additionLog.add(p.toString());
            removalLog.add(oldPe.get().toString());
        }

        // Finally, add to Json
        if (!removalLog.isEmpty()) {
            ((ObjectNode) auditJson).put("Removed " + MapType.valueOfTypeId(otherMapTypeId, true).toLowerCase(), StringUtils.join(removalLog, System.getProperty("line.separator")));
        }
        if (!additionLog.isEmpty()) {
            ((ObjectNode) auditJson).put("Added " + MapType.valueOfTypeId(otherMapTypeId, true).toLowerCase(), StringUtils.join(additionLog, System.getProperty("line.separator")));
        }

        return auditJson;
    }

    private JsonNode updateMappingsAndGetAuditForDocuments(String parentItem, List<String> oldDocuments,
                                               List<JsonDocumentation> newDocuments, Short thisMapTypeId,
                                               JsonNode auditJson, EntityManager entityManager) throws Exception {

        List<String> updatedMappings = new ArrayList<String>();
        List<String> removedMappings = new ArrayList<>();
        List<String> addedMappings = new ArrayList<>();
        newDocuments.forEach((k) -> updatedMappings.add(k.getUuid()));

        Short documentMapType = MapType.DOCUMENT.getMapType();

        updateMappings(false, parentItem, oldDocuments, updatedMappings,
                thisMapTypeId, documentMapType, removedMappings, addedMappings, entityManager);

        deleteDocuments(removedMappings);
        saveDocuments(addedMappings, newDocuments);

        auditJson = appendToJson(getChangeDescription(false, false, thisMapTypeId, documentMapType),
                removedMappings, MapType.valueOfTypeId(documentMapType), auditJson);
        auditJson = appendToJson(getChangeDescription(false, true, thisMapTypeId, documentMapType),
                addedMappings, MapType.valueOfTypeId(documentMapType), auditJson);

        return auditJson;
    }

    private String getChangeDescription(boolean thisItemIsChild, boolean added, Short thisMapTypeId, Short otherMapTypeId) {
        List<String> components = new ArrayList<>();

        components.add(added ? "Added" : "Removed");
        if (thisMapTypeId.equals(otherMapTypeId)) {
            components.add(thisItemIsChild ? "parent" : "child");
        }
        if (thisMapTypeId.equals(MapType.PUBLISHER.getMapType())) {
            components.add("publishing");
        }
        if (thisMapTypeId.equals(MapType.SUBSCRIBER.getMapType())) {
            components.add("subscribing");
        }
        components.add(MapType.valueOfTypeId(otherMapTypeId, true).toLowerCase());

        return String.join(" ", components);
    }

    private void deleteDocuments(List<String> deletedDocuments) throws Exception {
        for (String uuid : deletedDocuments) {
            new DocumentationDAL().deleteDocument(uuid);
        }
    }

    private void saveDocuments(List<String> addedDocuments, List<JsonDocumentation> documents) throws Exception {
        for (JsonDocumentation doc : documents) {
            if (addedDocuments.contains(doc.getUuid())) {
                if (doc.getUuid() != null) {
                    new DocumentationDAL().updateDocument(doc);
                } else {
                    doc.setUuid(UUID.randomUUID().toString());
                    new DocumentationDAL().saveDocument(doc);
                }
            }
        }
    }


    public void bulkSaveMappings(List<MasterMappingEntity> mappings) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            int batchSize = 50;
            entityManager.getTransaction().begin();

            for (int i = 0; i < mappings.size(); ++i) {
                MasterMappingEntity mapping = mappings.get(i);
                entityManager.merge(mapping);
                if (i % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    private JsonNode updateMappingsAndGetAuditForSchedule(String projectUuid, ProjectScheduleEntity scheduleEntity,
                                                          JsonProjectSchedule scheduleJson, Short thisMapTypeId,
                                                          JsonNode auditJson, EntityManager entityManager) throws Exception {

        SecurityProjectScheduleDAL dal = new SecurityProjectScheduleDAL();
        //schedule was set
        List<String> parent = new ArrayList<>();
        parent.add(projectUuid);
        if (scheduleJson != null) {
            //No existing schedule
            if (scheduleEntity == null) {
                dal.save(scheduleJson);
                saveMappings(true, scheduleJson.getUuid(), parent, MapType.SCHEDULE.getMapType(),
                        MapType.PROJECT.getMapType(), entityManager);
                ((ObjectNode)auditJson).put("AddedSCHEDULE",
                        StringUtils.join(scheduleJson.getCronDescription() + " ("+scheduleJson.getUuid()+")",
                                System.getProperty("line.separator")));
            } else {
                //Schedule was just updated, no need to do anything with the mappings
                if (scheduleEntity.getUuid().equals(scheduleJson.getUuid())) {
                    dal.update(scheduleJson);
                    ((ObjectNode)auditJson).put("RemovedSCHEDULE",
                            StringUtils.join(scheduleEntity.getCronDescription() + " ("+scheduleEntity.getUuid()+")",
                                    System.getProperty("line.separator")));
                    ((ObjectNode)auditJson).put("AddedSCHEDULE",
                            StringUtils.join(scheduleJson.getCronDescription() + " ("+scheduleJson.getUuid()+")",
                                    System.getProperty("line.separator")));
                } else {
                    //Previous schedule was deleted then a new one was added
                    dal.delete(scheduleEntity.getUuid());
                    deleteMappings(true, scheduleEntity.getUuid(), parent, MapType.SCHEDULE.getMapType(),
                            thisMapTypeId, entityManager);
                    ((ObjectNode)auditJson).put("RemovedSCHEDULE",
                            StringUtils.join(scheduleEntity.getCronDescription() + " ("+scheduleEntity.getUuid()+")",
                                    System.getProperty("line.separator")));

                    dal.save(scheduleJson);
                    saveMappings(true, scheduleJson.getUuid(), parent, MapType.SCHEDULE.getMapType(),
                            MapType.PROJECT.getMapType(), entityManager);
                    ((ObjectNode)auditJson).put("AddedSCHEDULE",
                            StringUtils.join(scheduleJson.getCronDescription() + " ("+scheduleJson.getUuid()+")",
                                    System.getProperty("line.separator")));
                }
            }
        } else if (scheduleEntity != null) {
            //schedule was deleted
            dal.delete(scheduleEntity.getUuid());
            deleteMappings(true, scheduleEntity.getUuid(), parent, MapType.SCHEDULE.getMapType(),
                    thisMapTypeId, entityManager);
            ((ObjectNode)auditJson).put("RemovedSCHEDULE",
                    StringUtils.join(scheduleEntity.getCronDescription() + " ("+scheduleEntity.getUuid()+")",
                            System.getProperty("line.separator")));
        }
        return auditJson;
    }

    private JsonNode updateMappingsAndGetAuditForExtractTechnicalDetails(String projectUuid, ExtractTechnicalDetailsEntity detailsEntity,
                                                          JsonExtractTechnicalDetails detailsJson, Short thisMapTypeId,
                                                          JsonNode auditJson, EntityManager entityManager) throws Exception {

        ExtractTechnicalDetailsDAL dal = new ExtractTechnicalDetailsDAL();
        //details were set
        List<String> parent = new ArrayList<>();
        parent.add(projectUuid);
        if (detailsJson != null) {
            //No existing details
            if (detailsEntity == null) {
                if (detailsJson.getUuid() == null) {
                    detailsJson.setUuid(UUID.randomUUID().toString());
                }
                dal.saveExtractTechnicalDetails(detailsJson);
                saveMappings(true, detailsJson.getUuid(), parent, MapType.EXTRACTTECHNICALDETAILS.getMapType(),
                        MapType.PROJECT.getMapType(), entityManager);
                ((ObjectNode)auditJson).put("AddedEXTRACTTECHNICALDETAILS",
                        StringUtils.join(detailsJson.getName() + " ("+detailsJson.getUuid()+")",
                                System.getProperty("line.separator")));
            } else {
                //Details were just updated, no need to do anything with the mappings
                if (detailsEntity.getUuid().equals(detailsJson.getUuid())) {
                    dal.updateExtractTechnicalDetails(detailsJson);
                    ((ObjectNode)auditJson).put("RemovedEXTRACTTECHINICALDETAILS",
                            StringUtils.join(detailsEntity.getName() + " ("+detailsEntity.getUuid()+")",
                                    System.getProperty("line.separator")));
                    ((ObjectNode)auditJson).put("AddedEXTRACTTECHNICALDETAILS",
                            StringUtils.join(detailsJson.getName() + " ("+detailsJson.getUuid()+")",
                                    System.getProperty("line.separator")));
                } else {
                    //Previous details were deleted then new ones were added
                    dal.deleteExtractTechnicalDetails(detailsEntity.getUuid());
                    deleteMappings(true, detailsEntity.getUuid(), parent, MapType.EXTRACTTECHNICALDETAILS.getMapType(),
                            thisMapTypeId, entityManager);
                    ((ObjectNode)auditJson).put("RemovedEXTRACTTECHNICALDETAILS",
                            StringUtils.join(detailsEntity.getName() + " ("+detailsEntity.getUuid()+")",
                                    System.getProperty("line.separator")));
                    dal.saveExtractTechnicalDetails(detailsJson);
                    saveMappings(true, detailsJson.getUuid(), parent, MapType.EXTRACTTECHNICALDETAILS.getMapType(),
                            MapType.PROJECT.getMapType(), entityManager);
                    ((ObjectNode)auditJson).put("AddedEXTRACTTECHNICALDETAILS",
                            StringUtils.join(detailsJson.getName() + " ("+detailsJson.getUuid()+")",
                                    System.getProperty("line.separator")));
                }
            }
        } else if (detailsEntity != null) {
            //details were deleted
            dal.deleteExtractTechnicalDetails(detailsEntity.getUuid());
            deleteMappings(true, detailsEntity.getUuid(), parent, MapType.EXTRACTTECHNICALDETAILS.getMapType(),
                    thisMapTypeId, entityManager);
            ((ObjectNode)auditJson).put("RemovedEXTRACTTECHNICALDETAILS",
                    StringUtils.join(detailsEntity.getName() + " ("+detailsEntity.getUuid()+")",
                            System.getProperty("line.separator")));
        }
        return auditJson;
    }
}
