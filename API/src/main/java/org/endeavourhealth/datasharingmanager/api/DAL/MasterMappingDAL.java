package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectScheduleDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.*;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.uiaudit.logic.AuditCompareLogic;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

public class MasterMappingDAL {
    
    private EntityManager _entityManager;

    public MasterMappingDAL(EntityManager entityManager) {
        _entityManager = entityManager;
    }

    public JsonNode updateDataSetMappings(JsonDataSet updatedDataSet, DatasetEntity oldDataset, JsonNode auditJson) throws Exception {
        String uuid = (updatedDataSet != null? updatedDataSet.getUuid(): oldDataset.getUuid());
        Short thisMapTypeID = MapType.DATASET.getMapType();

        // DPAs
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldDataset == null ? null : oldDataset.getDpas()),
                (updatedDataSet == null ? null : updatedDataSet.getDpas()),
                thisMapTypeID, MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson);

        return auditJson;
    }

    public JsonNode updateCohortMappings(JsonCohort updatedCohort, CohortEntity oldCohort, JsonNode auditJson) throws Exception {
        String uuid = (updatedCohort != null ? updatedCohort.getUuid() : oldCohort.getUuid());
        Short thisMapTypeID = MapType.COHORT.getMapType();

        // DPAs
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldCohort == null ? null : oldCohort.getDpas()),
                (updatedCohort == null ? null : updatedCohort.getDpas()), thisMapTypeID, MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson);

        return auditJson;

    }

    public JsonNode updateDataProcessingAgreementMappings(JsonDPA updatedDPA, DataProcessingAgreementEntity oldDPA, JsonNode auditJson) throws Exception {
        String uuid = (updatedDPA != null ? updatedDPA.getUuid() : oldDPA.getUuid());
        Short thisMapTypeID = MapType.DATAPROCESSINGAGREEMENT.getMapType();

        // Purposes
        auditJson = updatePurposesAndGetAudit(uuid, (oldDPA == null ? null : oldDPA.getPurposes()),
                (updatedDPA == null ? null : updatedDPA.getPurposes()), thisMapTypeID, MapType.PURPOSE.getMapType(), auditJson);

        // Benefits
        auditJson = updatePurposesAndGetAudit(uuid, (oldDPA == null ? null : oldDPA.getBenefits()),
                (updatedDPA == null ? null : updatedDPA.getBenefits()), thisMapTypeID, MapType.BENEFIT.getMapType(), auditJson);

        // Regions
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldDPA == null ? null : oldDPA.getRegions()),
                (updatedDPA == null ? null : updatedDPA.getRegions()), thisMapTypeID, MapType.REGION.getMapType(), auditJson);

        // Publishers
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldDPA == null ? null : oldDPA.getPublishers()),
                (updatedDPA == null ? null : updatedDPA.getPublishers()), thisMapTypeID, MapType.PUBLISHER.getMapType(), auditJson);

        // Documentation
        auditJson = updateMappingsAndGetAuditForObjList(false, uuid, (oldDPA == null ? null : oldDPA.getDocumentations()),
                (updatedDPA == null ? null : updatedDPA.getDocumentations()), thisMapTypeID, MapType.DOCUMENT.getMapType(), auditJson);

        return auditJson;
    }


    public JsonNode updateDataSharingAgreementMappings(JsonDSA updatedDSA, DataSharingAgreementEntity oldDSA, JsonNode auditJson) throws Exception {
        String uuid = (updatedDSA != null ? updatedDSA.getUuid() : oldDSA.getUuid());
        Short thisMapTypeID = MapType.DATASHARINGAGREEMENT.getMapType();

        // Purposes
        auditJson = updatePurposesAndGetAudit(uuid, (oldDSA == null ? null : oldDSA.getPurposes()),
                (updatedDSA == null ? null : updatedDSA.getPurposes()), thisMapTypeID, MapType.PURPOSE.getMapType(), auditJson);

        // Benefits
        auditJson = updatePurposesAndGetAudit(uuid, (oldDSA == null ? null : oldDSA.getBenefits()),
                (updatedDSA == null ? null : updatedDSA.getBenefits()), thisMapTypeID, MapType.BENEFIT.getMapType(), auditJson);

        // Regions
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldDSA == null ? null : oldDSA.getRegions()),
                (updatedDSA == null ? null : updatedDSA.getRegions()), thisMapTypeID, MapType.REGION.getMapType(), auditJson);

        // Projects
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldDSA == null ? null : oldDSA.getProjects()),
                (updatedDSA == null ? null : updatedDSA.getProjects()), thisMapTypeID, MapType.PROJECT.getMapType(), auditJson);

        // Publishers
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldDSA == null ? null : oldDSA.getPublishers()),
                (updatedDSA == null ? null : updatedDSA.getPublishers()), thisMapTypeID, MapType.PUBLISHER.getMapType(), auditJson);

        // Subscribers
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldDSA == null ? null : oldDSA.getSubscribers()),
                (updatedDSA == null ? null : updatedDSA.getSubscribers()), thisMapTypeID, MapType.SUBSCRIBER.getMapType(), auditJson);

        // Documentation
        auditJson = updateMappingsAndGetAuditForObjList(false, uuid, (oldDSA == null ? null : oldDSA.getDocumentations()),
                (updatedDSA == null ? null : updatedDSA.getDocumentations()), thisMapTypeID, MapType.DOCUMENT.getMapType(), auditJson);

        return auditJson;
    }

    public JsonNode updateRegionMappings(JsonRegion updatedRegion, RegionEntity oldRegion, JsonNode auditJson) throws Exception {
        String uuid = (updatedRegion != null ? updatedRegion.getUuid() : oldRegion.getUuid());
        Short thisMapTypeID = MapType.REGION.getMapType();

        // DSAs
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldRegion == null ? null : oldRegion.getSharingAgreements()),
                (updatedRegion == null ? null : updatedRegion.getSharingAgreements()), thisMapTypeID, MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);

        // DPAs
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldRegion == null ? null : oldRegion.getProcessingAgreements()),
                (updatedRegion == null ? null : updatedRegion.getProcessingAgreements()), thisMapTypeID, MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson);

        // Parent Regions
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldRegion == null ? null : oldRegion.getParentRegions()),
                (updatedRegion == null ? null : updatedRegion.getParentRegions()), thisMapTypeID, thisMapTypeID, auditJson);

        // Child Regions
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldRegion == null ? null : oldRegion.getChildRegions()),
                (updatedRegion == null ? null : updatedRegion.getChildRegions()), thisMapTypeID, thisMapTypeID, auditJson);

        // Organisations
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldRegion == null ? null : oldRegion.getOrganisations()),
                (updatedRegion == null ? null : updatedRegion.getOrganisations()), thisMapTypeID, MapType.ORGANISATION.getMapType(), auditJson);

        return auditJson;
    }

    public JsonNode updateOrganisationMappings(JsonOrganisation updatedOrganisation, OrganisationEntity oldOrganisation, JsonNode auditJson) throws Exception {
        String uuid = (updatedOrganisation != null ? updatedOrganisation.getUuid() : oldOrganisation.getUuid());
        Short thisMapTypeID = MapType.ORGANISATION.getMapType();

        // Regions
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getRegions()),
                (updatedOrganisation == null ? null : updatedOrganisation.getRegions()), thisMapTypeID, MapType.REGION.getMapType(), auditJson);

        // Parent Organisations
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getParentOrganisations()),
                (updatedOrganisation == null ? null : updatedOrganisation.getParentOrganisations()), thisMapTypeID, thisMapTypeID, auditJson);

        // Child Organisations
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldOrganisation == null ? null : oldOrganisation.getChildOrganisations()),
                (updatedOrganisation == null ? null : updatedOrganisation.getChildOrganisations()), thisMapTypeID, thisMapTypeID, auditJson);

        // Services
        auditJson = updateMappingsAndGetAudit(false, uuid, (oldOrganisation == null ? null : oldOrganisation.getServices()),
                (updatedOrganisation == null ? null : updatedOrganisation.getServices()), thisMapTypeID, MapType.SERVICE.getMapType(), auditJson);

        // Publishing DPA
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getDpaPublishing()),
                (updatedOrganisation == null ? null : updatedOrganisation.getDpaPublishing()), MapType.PUBLISHER.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson);

        // Publishing DSA
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getDsaPublishing()),
                (updatedOrganisation == null ? null : updatedOrganisation.getDsaPublishing()), MapType.PUBLISHER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);

        // Subscribing DSA
        auditJson = updateMappingsAndGetAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getDsaSubscribing()),
                (updatedOrganisation == null ? null : updatedOrganisation.getDsaSubscribing()), MapType.SUBSCRIBER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);

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
                                List<String> removedMappings, List<String> addedMappings) throws Exception {

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
                    deleteMappings(thisItemIsChild, thisItem, removedMappings, thisMapTypeId, otherMapTypeId);
                }

                if (!addedMappings.isEmpty()) {
                    saveMappings(thisItemIsChild, thisItem, addedMappings, thisMapTypeId, otherMapTypeId);
                }

            } catch (Exception e) {
                throw e;
            }
        }
    }

    private void saveMappings(boolean thisItemIsChild, String thisItem, List<String> mappingsToAdd, Short thisMapTypeId, Short otherMapTypeId) throws Exception {
        mappingsToAdd.forEach((mapping) -> {
            MasterMappingEntity mme;
            if (thisItemIsChild) {
                mme = new MasterMappingEntity(thisItem, thisMapTypeId, mapping, otherMapTypeId);
            } else {
                mme = new MasterMappingEntity(mapping, otherMapTypeId, thisItem, thisMapTypeId);
            }
            _entityManager.persist(mme);
        });
    }


    private void deleteMappings(boolean thisItemIsChild, String thisItem, List<String> mappingsToDelete, Short thisMapTypeId, Short otherMapTypeId) throws Exception {
        mappingsToDelete.forEach((mapping) -> {
            MasterMappingEntity mme;
            if (thisItemIsChild) {
                mme = new MasterMappingEntity(thisItem, thisMapTypeId, mapping, otherMapTypeId);
            } else {
                mme = new MasterMappingEntity(mapping, otherMapTypeId, thisItem, thisMapTypeId);
            }
            _entityManager.remove(_entityManager.merge(mme));
        });
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

    public JsonNode updateProjectMappings(JsonProject updatedProject, ProjectEntity oldProject, JsonNode auditJson) throws Exception {
        String uuid = (updatedProject != null ? updatedProject.getUuid() : oldProject.getUuid());
        Short thisMapTypeID = MapType.PROJECT.getMapType();

        // Publishers
        auditJson = updateMappingsAndGetAudit(false, uuid, oldProject.getPublishers(),
                updatedProject.getPublishers(), thisMapTypeID, MapType.PUBLISHER.getMapType(), auditJson);

        // Subscriber
        auditJson = updateMappingsAndGetAudit(false, uuid, oldProject.getSubscribers(),
                updatedProject.getSubscribers(), thisMapTypeID, MapType.SUBSCRIBER.getMapType(), auditJson);

        // Cohorts
        auditJson = updateMappingsAndGetAudit(false, uuid, oldProject.getCohorts(),
                updatedProject.getCohorts(), thisMapTypeID, MapType.COHORT.getMapType(), auditJson);

        // DataSets
        auditJson = updateMappingsAndGetAudit(false, uuid, oldProject.getDataSets(),
                updatedProject.getDataSets(), thisMapTypeID, MapType.DATASET.getMapType(), auditJson);

        // DSA
        auditJson = updateMappingsAndGetAudit(true, uuid, oldProject.getDsas(),
                updatedProject.getDsas(), thisMapTypeID, MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);

        // Documents
        auditJson = updateMappingsAndGetAuditForDocuments(uuid, oldProject.getDocumentations(),
                updatedProject.getDocumentations(), thisMapTypeID, auditJson);

        // Extract Technical Details
        auditJson = updateMappingsAndGetAuditForExtractTechnicalDetails(uuid, oldProject.getExtractTechnicalDetails(),
                updatedProject.getExtractTechnicalDetails(), thisMapTypeID, auditJson);

        //Schedules
        auditJson = updateMappingsAndGetAuditForSchedule(uuid, oldProject.getSchedule(),
                updatedProject.getSchedule(), thisMapTypeID, auditJson);

        return auditJson;
    }

    private <T extends JsonItem> JsonNode updateMappingsAndGetAuditForObjList(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                               List<T> updatedObjectList, Short thisMapTypeId, Short otherMapTypeId,
                                               JsonNode auditJson) throws Exception {
        List<String> updatedMappings = new ArrayList<String>();
        if (updatedObjectList != null) {
            updatedObjectList.forEach((o) -> updatedMappings.add(o.getUuid()));
        }

        return updateMappingsAndGetAudit(thisItemIsChild, thisItem, oldMappings, updatedMappings, thisMapTypeId, otherMapTypeId, auditJson);
    }

    private JsonNode updateMappingsAndGetAudit(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                               Map<UUID, String> updatedMap, Short thisMapTypeId, Short otherMapTypeId,
                                               JsonNode auditJson) throws Exception {
        List<String> updatedMappings = new ArrayList<String>();
        if (updatedMap != null) {
            updatedMap.forEach((k, v) -> updatedMappings.add(k.toString()));
        }

        return updateMappingsAndGetAudit(thisItemIsChild, thisItem, oldMappings, updatedMappings, thisMapTypeId, otherMapTypeId, auditJson);
    }

    private JsonNode updateMappingsAndGetAudit(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                               List<String> updatedMappings, Short thisMapTypeId, Short otherMapTypeId,
                                               JsonNode auditJson) throws Exception {

        List<String> removedMappings = new ArrayList<>();
        List<String> addedMappings = new ArrayList<>();

        updateMappings(thisItemIsChild, thisItem, oldMappings, updatedMappings,
                thisMapTypeId, otherMapTypeId, removedMappings, addedMappings);

        auditJson = appendToJson(getChangeDescription(thisItemIsChild, false, thisMapTypeId, otherMapTypeId),
                removedMappings, MapType.valueOfTypeId(otherMapTypeId), auditJson);
        auditJson = appendToJson(getChangeDescription(thisItemIsChild, true, thisMapTypeId, otherMapTypeId),
                addedMappings, MapType.valueOfTypeId(otherMapTypeId), auditJson);

        return auditJson;
    }

    private JsonNode updatePurposesAndGetAudit(String thisItem, List<PurposeEntity> oldPurposes,
                                               List<JsonPurpose> updatedPurposes, Short thisMapTypeId, Short otherMapTypeId,
                                               JsonNode auditJson) throws Exception {

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
            deleteMappings(false, thisItem, removedPurposeUuids, thisMapTypeId, otherMapTypeId);
            removedPurposes.forEach(rp -> removalLog.add(rp.toString()));
        }

        if (!addedPurposes.isEmpty()) {
            List<String> addedPurposeUuids = addedPurposes.stream().map(JsonPurpose::getUuid).collect(Collectors.toList());
            saveMappings(false, thisItem, addedPurposeUuids, thisMapTypeId, otherMapTypeId);
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
            ((ObjectNode) auditJson).put("Removed " + MapType.valueOfTypeId(otherMapTypeId, true).toLowerCase(),
                    StringUtils.join(removalLog, System.getProperty("line.separator")));
        }
        if (!additionLog.isEmpty()) {
            ((ObjectNode) auditJson).put("Added " + MapType.valueOfTypeId(otherMapTypeId, true).toLowerCase(),
                    StringUtils.join(additionLog, System.getProperty("line.separator")));
        }

        return auditJson;
    }

    private JsonNode updateMappingsAndGetAuditForDocuments(String parentItem, List<String> oldDocuments,
                                               List<JsonDocumentation> newDocuments, Short thisMapTypeId,
                                               JsonNode auditJson) throws Exception {

        List<String> updatedMappings = new ArrayList<String>();
        List<String> removedMappings = new ArrayList<>();
        List<String> addedMappings = new ArrayList<>();
        newDocuments.forEach((k) -> updatedMappings.add(k.getUuid()));

        Short documentMapType = MapType.DOCUMENT.getMapType();

        updateMappings(false, parentItem, oldDocuments, updatedMappings,
                thisMapTypeId, documentMapType, removedMappings, addedMappings);

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
                                                          JsonNode auditJson) throws Exception {

        SecurityProjectScheduleDAL dal = new SecurityProjectScheduleDAL();
        //schedule was set
        List<String> parent = new ArrayList<>();
        parent.add(projectUuid);
        if (scheduleJson != null) {
            //No existing schedule
            if (scheduleEntity == null) {
                dal.save(scheduleJson);
                saveMappings(true, scheduleJson.getUuid(), parent, MapType.SCHEDULE.getMapType(),
                        MapType.PROJECT.getMapType());
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
                            thisMapTypeId);
                    ((ObjectNode)auditJson).put("RemovedSCHEDULE",
                            StringUtils.join(scheduleEntity.getCronDescription() + " ("+scheduleEntity.getUuid()+")",
                                    System.getProperty("line.separator")));

                    dal.save(scheduleJson);
                    saveMappings(true, scheduleJson.getUuid(), parent, MapType.SCHEDULE.getMapType(),
                            MapType.PROJECT.getMapType());
                    ((ObjectNode)auditJson).put("AddedSCHEDULE",
                            StringUtils.join(scheduleJson.getCronDescription() + " ("+scheduleJson.getUuid()+")",
                                    System.getProperty("line.separator")));
                }
            }
        } else if (scheduleEntity != null) {
            //schedule was deleted
            dal.delete(scheduleEntity.getUuid());
            deleteMappings(true, scheduleEntity.getUuid(), parent, MapType.SCHEDULE.getMapType(),
                    thisMapTypeId);
            ((ObjectNode)auditJson).put("RemovedSCHEDULE",
                    StringUtils.join(scheduleEntity.getCronDescription() + " ("+scheduleEntity.getUuid()+")",
                            System.getProperty("line.separator")));
        }
        return auditJson;
    }

    private JsonNode updateMappingsAndGetAuditForExtractTechnicalDetails(String projectUuid, ExtractTechnicalDetailsEntity detailsEntity,
                                                          JsonExtractTechnicalDetails detailsJson, Short thisMapTypeId,
                                                          JsonNode auditJson) throws Exception {

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
                        MapType.PROJECT.getMapType());
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
                            thisMapTypeId);
                    ((ObjectNode)auditJson).put("RemovedEXTRACTTECHNICALDETAILS",
                            StringUtils.join(detailsEntity.getName() + " ("+detailsEntity.getUuid()+")",
                                    System.getProperty("line.separator")));
                    dal.saveExtractTechnicalDetails(detailsJson);
                    saveMappings(true, detailsJson.getUuid(), parent, MapType.EXTRACTTECHNICALDETAILS.getMapType(),
                            MapType.PROJECT.getMapType());
                    ((ObjectNode)auditJson).put("AddedEXTRACTTECHNICALDETAILS",
                            StringUtils.join(detailsJson.getName() + " ("+detailsJson.getUuid()+")",
                                    System.getProperty("line.separator")));
                }
            }
        } else if (detailsEntity != null) {
            //details were deleted
            dal.deleteExtractTechnicalDetails(detailsEntity.getUuid());
            deleteMappings(true, detailsEntity.getUuid(), parent, MapType.EXTRACTTECHNICALDETAILS.getMapType(),
                    thisMapTypeId);
            ((ObjectNode)auditJson).put("RemovedEXTRACTTECHNICALDETAILS",
                    StringUtils.join(detailsEntity.getName() + " ("+detailsEntity.getUuid()+")",
                            System.getProperty("line.separator")));
        }
        return auditJson;
    }
}
