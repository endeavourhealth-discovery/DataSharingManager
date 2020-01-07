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

    void updateDataSetMappings(JsonDataSet updatedDataSet, DatasetEntity oldDataset, JsonNode auditJson) throws Exception {
        String uuid = (updatedDataSet != null? updatedDataSet.getUuid(): oldDataset.getUuid());
        Short thisMapTypeID = MapType.DATASET.getMapType();

        // DPAs
        updateMappingsAndAddToAudit(true, uuid, (oldDataset == null ? null : oldDataset.getDpas()),
                (updatedDataSet == null ? null : updatedDataSet.getDpas()),
                thisMapTypeID, MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson);
    }

    void updateCohortMappings(JsonCohort updatedCohort, CohortEntity oldCohort, JsonNode auditJson) throws Exception {
        String uuid = (updatedCohort != null ? updatedCohort.getUuid() : oldCohort.getUuid());
        Short thisMapTypeID = MapType.COHORT.getMapType();

        // DPAs
        updateMappingsAndAddToAudit(true, uuid, (oldCohort == null ? null : oldCohort.getDpas()),
                (updatedCohort == null ? null : updatedCohort.getDpas()), thisMapTypeID, MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson);
    }

    void updateDataProcessingAgreementMappings(JsonDPA updatedDPA, DataProcessingAgreementEntity oldDPA, JsonNode auditJson) throws Exception {
        String uuid = (updatedDPA != null ? updatedDPA.getUuid() : oldDPA.getUuid());
        Short thisMapTypeID = MapType.DATAPROCESSINGAGREEMENT.getMapType();

        // Purposes
        updatePurposesAndGetAudit(uuid, (oldDPA == null ? null : oldDPA.getPurposes()),
                (updatedDPA == null ? null : updatedDPA.getPurposes()), thisMapTypeID, MapType.PURPOSE.getMapType(), auditJson);

        // Benefits
        updatePurposesAndGetAudit(uuid, (oldDPA == null ? null : oldDPA.getBenefits()),
                (updatedDPA == null ? null : updatedDPA.getBenefits()), thisMapTypeID, MapType.BENEFIT.getMapType(), auditJson);

        // Regions
        updateMappingsAndAddToAudit(true, uuid, (oldDPA == null ? null : oldDPA.getRegions()),
                (updatedDPA == null ? null : updatedDPA.getRegions()), thisMapTypeID, MapType.REGION.getMapType(), auditJson);

        // Publishers
        updateMappingsAndAddToAudit(false, uuid, (oldDPA == null ? null : oldDPA.getPublishers()),
                (updatedDPA == null ? null : updatedDPA.getPublishers()), thisMapTypeID, MapType.PUBLISHER.getMapType(), auditJson);

        // Documentation
        updateDocumentsAndAddToAudit(uuid, (oldDPA == null ? null : oldDPA.getDocumentations()),
                (updatedDPA == null ? null : updatedDPA.getDocumentations()), thisMapTypeID, auditJson);
    }


    void updateDataSharingAgreementMappings(JsonDSA updatedDSA, DataSharingAgreementEntity oldDSA, JsonNode auditJson) throws Exception {
        String uuid = (updatedDSA != null ? updatedDSA.getUuid() : oldDSA.getUuid());
        Short thisMapTypeID = MapType.DATASHARINGAGREEMENT.getMapType();

        // Purposes
        updatePurposesAndGetAudit(uuid, (oldDSA == null ? null : oldDSA.getPurposes()),
                (updatedDSA == null ? null : updatedDSA.getPurposes()), thisMapTypeID, MapType.PURPOSE.getMapType(), auditJson);

        // Benefits
        updatePurposesAndGetAudit(uuid, (oldDSA == null ? null : oldDSA.getBenefits()),
                (updatedDSA == null ? null : updatedDSA.getBenefits()), thisMapTypeID, MapType.BENEFIT.getMapType(), auditJson);

        // Regions
        updateMappingsAndAddToAudit(true, uuid, (oldDSA == null ? null : oldDSA.getRegions()),
                (updatedDSA == null ? null : updatedDSA.getRegions()), thisMapTypeID, MapType.REGION.getMapType(), auditJson);

        // Projects
        updateMappingsAndAddToAudit(false, uuid, (oldDSA == null ? null : oldDSA.getProjects()),
                (updatedDSA == null ? null : updatedDSA.getProjects()), thisMapTypeID, MapType.PROJECT.getMapType(), auditJson);

        // Publishers
        updateMappingsAndAddToAudit(false, uuid, (oldDSA == null ? null : oldDSA.getPublishers()),
                (updatedDSA == null ? null : updatedDSA.getPublishers()), thisMapTypeID, MapType.PUBLISHER.getMapType(), auditJson);

        // Subscribers
        updateMappingsAndAddToAudit(false, uuid, (oldDSA == null ? null : oldDSA.getSubscribers()),
                (updatedDSA == null ? null : updatedDSA.getSubscribers()), thisMapTypeID, MapType.SUBSCRIBER.getMapType(), auditJson);

        // Documentation
        updateDocumentsAndAddToAudit(uuid, (oldDSA == null ? null : oldDSA.getDocumentations()),
                (updatedDSA == null ? null : updatedDSA.getDocumentations()), thisMapTypeID, auditJson);
    }

    void updateRegionMappings(JsonRegion updatedRegion, RegionEntity oldRegion, JsonNode auditJson) throws Exception {
        String uuid = (updatedRegion != null ? updatedRegion.getUuid() : oldRegion.getUuid());
        Short thisMapTypeID = MapType.REGION.getMapType();

        // DSAs
        updateMappingsAndAddToAudit(false, uuid, (oldRegion == null ? null : oldRegion.getSharingAgreements()),
                (updatedRegion == null ? null : updatedRegion.getSharingAgreements()), thisMapTypeID, MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);

        // DPAs
        updateMappingsAndAddToAudit(false, uuid, (oldRegion == null ? null : oldRegion.getProcessingAgreements()),
                (updatedRegion == null ? null : updatedRegion.getProcessingAgreements()), thisMapTypeID, MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson);

        // Parent Regions
        updateMappingsAndAddToAudit(true, uuid, (oldRegion == null ? null : oldRegion.getParentRegions()),
                (updatedRegion == null ? null : updatedRegion.getParentRegions()), thisMapTypeID, thisMapTypeID, auditJson);

        // Child Regions
        updateMappingsAndAddToAudit(false, uuid, (oldRegion == null ? null : oldRegion.getChildRegions()),
                (updatedRegion == null ? null : updatedRegion.getChildRegions()), thisMapTypeID, thisMapTypeID, auditJson);

        // Organisations
        updateMappingsAndAddToAudit(false, uuid, (oldRegion == null ? null : oldRegion.getOrganisations()),
                (updatedRegion == null ? null : updatedRegion.getOrganisations()), thisMapTypeID, MapType.ORGANISATION.getMapType(), auditJson);
    }

    void updateOrganisationMappings(JsonOrganisation updatedOrganisation, OrganisationEntity oldOrganisation, JsonNode auditJson) throws Exception {
        String uuid = (updatedOrganisation != null ? updatedOrganisation.getUuid() : oldOrganisation.getUuid());
        Short thisMapTypeID = MapType.ORGANISATION.getMapType();

        // Regions
        updateMappingsAndAddToAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getRegions()),
                (updatedOrganisation == null ? null : updatedOrganisation.getRegions()), thisMapTypeID, MapType.REGION.getMapType(), auditJson);

        // Parent Organisations
        updateMappingsAndAddToAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getParentOrganisations()),
                (updatedOrganisation == null ? null : updatedOrganisation.getParentOrganisations()), thisMapTypeID, thisMapTypeID, auditJson);

        // Child Organisations
        updateMappingsAndAddToAudit(false, uuid, (oldOrganisation == null ? null : oldOrganisation.getChildOrganisations()),
                (updatedOrganisation == null ? null : updatedOrganisation.getChildOrganisations()), thisMapTypeID, thisMapTypeID, auditJson);

        // Services
        updateMappingsAndAddToAudit(false, uuid, (oldOrganisation == null ? null : oldOrganisation.getServices()),
                (updatedOrganisation == null ? null : updatedOrganisation.getServices()), thisMapTypeID, MapType.SERVICE.getMapType(), auditJson);

        // Publishing DPA
        updateMappingsAndAddToAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getDpaPublishing()),
                (updatedOrganisation == null ? null : updatedOrganisation.getDpaPublishing()), MapType.PUBLISHER.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson);

        // Publishing DSA
        updateMappingsAndAddToAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getDsaPublishing()),
                (updatedOrganisation == null ? null : updatedOrganisation.getDsaPublishing()), MapType.PUBLISHER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);

        // Subscribing DSA
        updateMappingsAndAddToAudit(true, uuid, (oldOrganisation == null ? null : oldOrganisation.getDsaSubscribing()),
                (updatedOrganisation == null ? null : updatedOrganisation.getDsaSubscribing()), MapType.SUBSCRIBER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);
    }

    private void saveMappings(boolean thisItemIsChild, String thisItem, List<String> mappingsToAdd, Short thisMapTypeId, Short otherMapTypeId) {
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


    private void deleteMappings(boolean thisItemIsChild, String thisItem, List<String> mappingsToDelete, Short thisMapTypeId, Short otherMapTypeId) {
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

    void updateProjectMappings(JsonProject updatedProject, ProjectEntity oldProject, JsonNode auditJson) throws Exception {
        String uuid = (updatedProject != null ? updatedProject.getUuid() : oldProject.getUuid());
        Short thisMapTypeID = MapType.PROJECT.getMapType();

        // Publishers
        updateMappingsAndAddToAudit(false, uuid, oldProject.getPublishers(),
                updatedProject.getPublishers(), thisMapTypeID, MapType.PUBLISHER.getMapType(), auditJson);

        // Subscriber
        updateMappingsAndAddToAudit(false, uuid, oldProject.getSubscribers(),
                updatedProject.getSubscribers(), thisMapTypeID, MapType.SUBSCRIBER.getMapType(), auditJson);

        // Cohorts
        updateMappingsAndAddToAudit(false, uuid, oldProject.getCohorts(),
                updatedProject.getCohorts(), thisMapTypeID, MapType.COHORT.getMapType(), auditJson);

        // DataSets
        updateMappingsAndAddToAudit(false, uuid, oldProject.getDataSets(),
                updatedProject.getDataSets(), thisMapTypeID, MapType.DATASET.getMapType(), auditJson);

        // DSA
        updateMappingsAndAddToAudit(true, uuid, oldProject.getDsas(),
                updatedProject.getDsas(), thisMapTypeID, MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);

        // Documents
        updateDocumentsAndAddToAudit(uuid, oldProject.getDocumentations(),
                updatedProject.getDocumentations(), thisMapTypeID, auditJson);

        // Extract Technical Details
        updateMappingsAndGetAuditForExtractTechnicalDetails(uuid, oldProject.getExtractTechnicalDetails(),
                updatedProject.getExtractTechnicalDetails(), thisMapTypeID, auditJson);

        //Schedules
        updateMappingsAndGetAuditForSchedule(uuid, oldProject.getSchedule(),
                updatedProject.getSchedule(), thisMapTypeID, auditJson);
    }

    private void updateDocumentsAndAddToAudit(String thisItem, List<String> oldDocuments, List<JsonDocumentation> updatedDocuments,
                                              Short thisMapTypeId, JsonNode auditJson) throws Exception {

        List<String> updatedMappings = new ArrayList<>();

        if (updatedDocuments != null) {
            for (JsonDocumentation updatedDocument : updatedDocuments) {
                if (updatedDocument.getUuid() == null) {
                    updatedDocument.setUuid(UUID.randomUUID().toString());
                    new DocumentationDAL(_entityManager).saveDocument(updatedDocument);
                }
            }

            updatedDocuments.forEach((o) -> updatedMappings.add(o.getUuid()));
        }

        updateMappingsAndAddToAudit(false, thisItem, oldDocuments, updatedMappings, thisMapTypeId, MapType.DOCUMENT.getMapType(), auditJson);
    }

    private void updateMappingsAndAddToAudit(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                             Map<UUID, String> updatedMap, Short thisMapTypeId, Short otherMapTypeId,
                                             JsonNode auditJson) throws Exception {
        List<String> updatedMappings = new ArrayList<>();
        if (updatedMap != null) {
            updatedMap.forEach((k, v) -> updatedMappings.add(k.toString()));
        }

        updateMappingsAndAddToAudit(thisItemIsChild, thisItem, oldMappings, updatedMappings, thisMapTypeId, otherMapTypeId, auditJson);
    }

    private void updateMappingsAndAddToAudit(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                             List<String> updatedMappings, Short thisMapTypeId, Short otherMapTypeId,
                                             JsonNode auditJson) throws Exception {

        List<String> removedMappings = new ArrayList<>();
        List<String> addedMappings = new ArrayList<>();

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

        if (!removedMappings.isEmpty()) {
            deleteMappings(thisItemIsChild, thisItem, removedMappings, thisMapTypeId, otherMapTypeId);
            auditJson = new AuditCompareLogic().generateListDifferenceAuditJson(auditJson,
                    getChangeDescription(thisItemIsChild, false, thisMapTypeId, otherMapTypeId),
                    removedMappings, MapType.valueOfTypeId(otherMapTypeId));
        }

        if (!addedMappings.isEmpty()) {
            saveMappings(thisItemIsChild, thisItem, addedMappings, thisMapTypeId, otherMapTypeId);
            auditJson = new AuditCompareLogic().generateListDifferenceAuditJson(auditJson,
                    getChangeDescription(thisItemIsChild, true, thisMapTypeId, otherMapTypeId),
                    addedMappings, MapType.valueOfTypeId(otherMapTypeId));
        }
    }

    private void updatePurposesAndGetAudit(String thisItem, List<PurposeEntity> oldPurposes,
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

    public void bulkSaveMappings(List<MasterMappingEntity> mappings) {
        try {
            int batchSize = 50;
            _entityManager.getTransaction().begin();

            for (int i = 0; i < mappings.size(); ++i) {
                MasterMappingEntity mapping = mappings.get(i);
                _entityManager.merge(mapping);
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

    private void updateMappingsAndGetAuditForSchedule(String projectUuid, ProjectScheduleEntity scheduleEntity,
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
    }

    private void updateMappingsAndGetAuditForExtractTechnicalDetails(String projectUuid, ExtractTechnicalDetailsEntity detailsEntity,
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
    }
}
