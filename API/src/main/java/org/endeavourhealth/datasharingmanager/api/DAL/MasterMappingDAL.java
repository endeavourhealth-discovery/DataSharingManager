package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectScheduleDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.*;
import org.endeavourhealth.uiaudit.logic.AuditCompareLogic;

import javax.persistence.EntityManager;
import java.util.*;

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
        if (updatedDPA != null && updatedDPA.getPurposes() != null) {
            updatePurposesAndAddToAudit(uuid, (oldDPA == null ? null : oldDPA.getPurposes()),
                    (updatedDPA == null ? null : updatedDPA.getPurposes()), thisMapTypeID, MapType.PURPOSE.getMapType(), auditJson);
        }

        // Benefits
        if (updatedDPA != null && updatedDPA.getBenefits() != null) {
            updatePurposesAndAddToAudit(uuid, (oldDPA == null ? null : oldDPA.getBenefits()),
                    (updatedDPA == null ? null : updatedDPA.getBenefits()), thisMapTypeID, MapType.BENEFIT.getMapType(), auditJson);
        }

        // Regions
        if (updatedDPA != null && updatedDPA.getRegions() != null) {
            updateMappingsAndAddToAudit(true, uuid, (oldDPA == null ? null : oldDPA.getRegions()),
                    (updatedDPA == null ? null : updatedDPA.getRegions()), thisMapTypeID, MapType.REGION.getMapType(), auditJson);
        }

        // Publishers
        if (updatedDPA != null && updatedDPA.getPublishers() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldDPA == null ? null : oldDPA.getPublishers()),
                    (updatedDPA == null ? null : updatedDPA.getPublishers()), thisMapTypeID, MapType.PUBLISHER.getMapType(), auditJson);
        }

        // Documentation
        if (updatedDPA != null && updatedDPA.getDocumentations() != null) {
            updateDocumentsAndAddToAudit(uuid, (oldDPA == null ? null : oldDPA.getDocumentations()),
                    (updatedDPA == null ? null : updatedDPA.getDocumentations()), thisMapTypeID, auditJson);
        }
    }


    void updateDataSharingAgreementMappings(JsonDSA updatedDSA, DataSharingAgreementEntity oldDSA, JsonNode auditJson) throws Exception {
        String uuid = (updatedDSA != null ? updatedDSA.getUuid() : oldDSA.getUuid());
        Short thisMapTypeID = MapType.DATASHARINGAGREEMENT.getMapType();

        // Purposes
        if (updatedDSA != null && updatedDSA.getPurposes() != null) {
            updatePurposesAndAddToAudit(uuid, (oldDSA == null ? null : oldDSA.getPurposes()),
                    (updatedDSA == null ? null : updatedDSA.getPurposes()), thisMapTypeID, MapType.PURPOSE.getMapType(), auditJson);
        }

        // Benefits
        if (updatedDSA != null && updatedDSA.getBenefits() != null) {
            updatePurposesAndAddToAudit(uuid, (oldDSA == null ? null : oldDSA.getBenefits()),
                    (updatedDSA == null ? null : updatedDSA.getBenefits()), thisMapTypeID, MapType.BENEFIT.getMapType(), auditJson);
        }

        // Regions
        if (updatedDSA != null && updatedDSA.getRegions() != null) {
            updateMappingsAndAddToAudit(true, uuid, (oldDSA == null ? null : oldDSA.getRegions()),
                    (updatedDSA == null ? null : updatedDSA.getRegions()), thisMapTypeID, MapType.REGION.getMapType(), auditJson);
        }

        // Projects
        if (updatedDSA != null && updatedDSA.getProjects() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldDSA == null ? null : oldDSA.getProjects()),
                    (updatedDSA == null ? null : updatedDSA.getProjects()), thisMapTypeID, MapType.PROJECT.getMapType(), auditJson);
        }

        // Publishers
        if (updatedDSA != null && updatedDSA.getPublishers() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldDSA == null ? null : oldDSA.getPublishers()),
                    (updatedDSA == null ? null : updatedDSA.getPublishers()), thisMapTypeID, MapType.PUBLISHER.getMapType(), auditJson);
        }

        // Subscribers
        if (updatedDSA != null && updatedDSA.getSubscribers() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldDSA == null ? null : oldDSA.getSubscribers()),
                    (updatedDSA == null ? null : updatedDSA.getSubscribers()), thisMapTypeID, MapType.SUBSCRIBER.getMapType(), auditJson);
        }

        // Documentation
        if (updatedDSA != null && updatedDSA.getDocumentations() != null) {
            updateDocumentsAndAddToAudit(uuid, (oldDSA == null ? null : oldDSA.getDocumentations()),
                    (updatedDSA == null ? null : updatedDSA.getDocumentations()), thisMapTypeID, auditJson);
        }
    }

    void updateRegionMappings(JsonRegion updatedRegion, RegionEntity oldRegion, JsonNode auditJson) throws Exception {
        String uuid = (updatedRegion != null ? updatedRegion.getUuid() : oldRegion.getUuid());
        Short thisMapTypeID = MapType.REGION.getMapType();

        // DSAs
        if (updatedRegion != null && updatedRegion.getSharingAgreements() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldRegion == null ? null : oldRegion.getSharingAgreements()),
                    (updatedRegion == null ? null : updatedRegion.getSharingAgreements()), thisMapTypeID, MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);
        }
        // DPAs
        if (updatedRegion != null && updatedRegion.getProcessingAgreements() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldRegion == null ? null : oldRegion.getProcessingAgreements()),
                    (updatedRegion == null ? null : updatedRegion.getProcessingAgreements()), thisMapTypeID, MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson);
        }
        // Parent Regions
        if (updatedRegion != null && updatedRegion.getParentRegions() != null) {
            updateMappingsAndAddToAudit(true, uuid, (oldRegion == null ? null : oldRegion.getParentRegions()),
                    (updatedRegion == null ? null : updatedRegion.getParentRegions()), thisMapTypeID, thisMapTypeID, auditJson);
        }
        // Child Regions
        if (updatedRegion != null && updatedRegion.getChildRegions() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldRegion == null ? null : oldRegion.getChildRegions()),
                    (updatedRegion == null ? null : updatedRegion.getChildRegions()), thisMapTypeID, thisMapTypeID, auditJson);
        }
        // Organisations
        if (updatedRegion != null && updatedRegion.getOrganisations() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldRegion == null ? null : oldRegion.getOrganisations()),
                    (updatedRegion == null ? null : updatedRegion.getOrganisations()), thisMapTypeID, MapType.ORGANISATION.getMapType(), auditJson);
        }
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

    void updateProjectMappings(JsonProject updatedProject, ProjectEntity oldProject, JsonNode auditJson) throws Exception {
        String uuid = (updatedProject != null ? updatedProject.getUuid() : oldProject.getUuid());
        Short thisMapTypeID = MapType.PROJECT.getMapType();

        // Publishers
        if (updatedProject != null && updatedProject.getPublishers() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldProject == null ? null : oldProject.getPublishers()),
                    (updatedProject == null ? null : updatedProject.getPublishers()), thisMapTypeID, MapType.PUBLISHER.getMapType(), auditJson);
        }
        // Subscriber
        if (updatedProject != null && updatedProject.getSubscribers() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldProject == null ? null : oldProject.getSubscribers()),
                    (updatedProject == null ? null : updatedProject.getSubscribers()), thisMapTypeID, MapType.SUBSCRIBER.getMapType(), auditJson);
        }
        // Cohorts
        if (updatedProject != null && updatedProject.getCohorts() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldProject == null ? null : oldProject.getCohorts()),
                    (updatedProject == null ? null : updatedProject.getCohorts()), thisMapTypeID, MapType.COHORT.getMapType(), auditJson);
        }
        // DataSets
        if (updatedProject != null && updatedProject.getDataSets() != null) {
            updateMappingsAndAddToAudit(false, uuid, (oldProject == null ? null : oldProject.getDataSets()),
                    (updatedProject == null ? null : updatedProject.getDataSets()), thisMapTypeID, MapType.DATASET.getMapType(), auditJson);
        }
        // DSA
        if (updatedProject != null && updatedProject.getDsas() != null) {
            updateMappingsAndAddToAudit(true, uuid, (oldProject == null ? null : oldProject.getDsas()),
                    (updatedProject == null ? null : updatedProject.getDsas()), thisMapTypeID, MapType.DATASHARINGAGREEMENT.getMapType(), auditJson);
        }
        // Documents
        if (updatedProject != null && updatedProject.getDocumentations() != null) {
            updateDocumentsAndAddToAudit(uuid, (oldProject == null ? null : oldProject.getDocumentations()),
                    (updatedProject == null ? null : updatedProject.getDocumentations()), thisMapTypeID, auditJson);
        }
        // Extract Technical Details
        updateExtractTechnicalDetailsAndAddToAudit(uuid, (oldProject == null ? null : oldProject.getExtractTechnicalDetails()),
                (updatedProject == null ? null : updatedProject.getExtractTechnicalDetails()), auditJson);

        //Schedules
        updateScheduleAndAddToAudit(uuid, (oldProject == null ? null : oldProject.getSchedule()),
                (updatedProject == null ? null : updatedProject.getSchedule()), auditJson);
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
                    buildChangeDescription(thisItemIsChild, false, thisMapTypeId, otherMapTypeId),
                    removedMappings, MapType.valueOfTypeId(otherMapTypeId));
        }

        if (!addedMappings.isEmpty()) {
            saveMappings(thisItemIsChild, thisItem, addedMappings, thisMapTypeId, otherMapTypeId);
            auditJson = new AuditCompareLogic().generateListDifferenceAuditJson(auditJson,
                    buildChangeDescription(thisItemIsChild, true, thisMapTypeId, otherMapTypeId),
                    addedMappings, MapType.valueOfTypeId(otherMapTypeId));
        }
    }

    private void updatePurposesAndAddToAudit(String thisItem, List<PurposeEntity> oldPurposes,
                                             List<JsonPurpose> updatedPurposes, Short thisMapTypeId, Short otherMapTypeId,
                                             JsonNode auditJson) {

        // First, identify what has changed
        List<JsonPurpose> addedPurposes = new ArrayList<>();
        List<PurposeEntity> removedPurposes = new ArrayList<>();

        List<String> changeLog = new ArrayList<>();

        if (oldPurposes != null) {
            for (PurposeEntity oldPurpose : oldPurposes) {
                if (updatedPurposes == null || updatedPurposes.stream().noneMatch(up -> up.getUuid().equals(oldPurpose.getUuid()))) {
                    removedPurposes.add(oldPurpose);
                }
            }
        }

        if (updatedPurposes != null) {
            for (JsonPurpose updatedPurpose : updatedPurposes) {
                updatedPurpose.setUuidIfRequired();

                if (oldPurposes == null) {
                    addedPurposes.add(updatedPurpose);
                } else {
                    Optional<PurposeEntity> oldPurpose = oldPurposes.stream().filter(op -> op.getUuid().equals(updatedPurpose.getUuid())).findFirst();
                    if (oldPurpose.isPresent()) {
                        if (!updatedPurpose.toString().equals(oldPurpose.get().toString())) {
                            changeLog.add(buildBeforeAfter(oldPurpose.get().toString(), updatedPurpose.toString()));
                            oldPurpose.get().updateFromJson(updatedPurpose);
                            _entityManager.merge(oldPurpose.get());
                        } //else: Unchanged - no action required.
                    } else {
                        addedPurposes.add(updatedPurpose);
                    }
                }
            }
        }

        // Now apply changes and add to Json
        if (!removedPurposes.isEmpty()) {
            List<String> removedPurposeUuids = new ArrayList<>();
            List<String> removalLog = new ArrayList<>();

            for (PurposeEntity removedPurpose : removedPurposes) {
                removedPurposeUuids.add(removedPurpose.getUuid());
                _entityManager.remove(_entityManager.merge(removedPurpose));
                removalLog.add(removedPurpose.toString());
            }

            deleteMappings(false, thisItem, removedPurposeUuids, thisMapTypeId, otherMapTypeId);
            ((ObjectNode) auditJson).put(buildChangeDescription(false, false, thisMapTypeId, otherMapTypeId),
                    StringUtils.join(removalLog, System.getProperty("line.separator")));
        }

        if (!addedPurposes.isEmpty()) {
            List<String> addedPurposeUuids = new ArrayList<>();
            List<String> additionLog = new ArrayList<>();

            for (JsonPurpose addedPurpose : addedPurposes) {
                addedPurposeUuids.add(addedPurpose.getUuid());
                _entityManager.persist(new PurposeEntity(addedPurpose));
                additionLog.add(addedPurpose.toString());
            }

            saveMappings(false, thisItem, addedPurposeUuids, thisMapTypeId, otherMapTypeId);
            ((ObjectNode) auditJson).put(buildChangeDescription(false, true, thisMapTypeId, otherMapTypeId),
                    StringUtils.join(additionLog, System.getProperty("line.separator")));
        }

        if (!changeLog.isEmpty()) {
            // No changes required to mappings, and entities already merged above

            ((ObjectNode) auditJson).put(buildChangeDescription(false, false, true, thisMapTypeId, otherMapTypeId),
                    StringUtils.join(changeLog, System.getProperty("line.separator")));
        }
    }

    private void updateScheduleAndAddToAudit(String thisItem, ProjectScheduleEntity oldSchedule,
                                             JsonProjectSchedule updatedSchedule, JsonNode auditJson) throws Exception {

        // First, identify what has changed.  Note that we only allow a single Schedule per Project
        JsonProjectSchedule addedSchedule = null;
        ProjectScheduleEntity removedSchedule = null;
        JsonProjectSchedule changedSchedule = null;

        if (oldSchedule == null) {
            if (updatedSchedule == null) {
                // No schedules at all - nothing to do
            } else {
                addedSchedule = updatedSchedule;
            }
        } else {
            if (updatedSchedule == null) {
                removedSchedule = oldSchedule;
            } else {
                if (updatedSchedule.getUuid().equals(oldSchedule.getUuid())) {
                    if (updatedSchedule.getCronDescription().equals(oldSchedule.getCronDescription())) {
                        // No change
                    } else {
                        changedSchedule = updatedSchedule;
                    }
                } else {
                    removedSchedule = oldSchedule;
                    addedSchedule = updatedSchedule;
                }
            }
        }

        SecurityProjectScheduleDAL securityProjectScheduleDAL = new SecurityProjectScheduleDAL();

        Short thisMapTypeId = MapType.PROJECT.getMapType();
        Short otherMapTypeId = MapType.SCHEDULE.getMapType();

        // Now apply changes
        if (removedSchedule != null) {
            securityProjectScheduleDAL.delete(removedSchedule.getUuid());

            List<String> removedScheduleUuids = new ArrayList<>(Arrays.asList(removedSchedule.getUuid()));
            deleteMappings(false, thisItem, removedScheduleUuids, thisMapTypeId, otherMapTypeId);

            ((ObjectNode) auditJson).put(buildChangeDescription(false, false, thisMapTypeId, otherMapTypeId),
                    removedSchedule.getCronDescription());
        }

        if (addedSchedule != null) {
            if (addedSchedule.getUuid() == null) {
                addedSchedule.setUuid(UUID.randomUUID().toString());
            }

            securityProjectScheduleDAL.save(addedSchedule);

            List<String> addedScheduleUuids = new ArrayList<>(Arrays.asList(addedSchedule.getUuid()));
            saveMappings(false, thisItem, addedScheduleUuids, thisMapTypeId, otherMapTypeId);

            ((ObjectNode) auditJson).put(buildChangeDescription(false, true, thisMapTypeId, otherMapTypeId),
                    addedSchedule.getCronDescription());
        }

        if (changedSchedule != null) {
            securityProjectScheduleDAL.update(changedSchedule);

            ((ObjectNode) auditJson).put(buildChangeDescription(false, false, true, thisMapTypeId, otherMapTypeId),
                    buildBeforeAfter(oldSchedule.getCronDescription(), changedSchedule.getCronDescription()));
        }
    }

    private void updateExtractTechnicalDetailsAndAddToAudit(String thisItem, ExtractTechnicalDetailsEntity oldETD,
                                             JsonExtractTechnicalDetails updatedETD, JsonNode auditJson) throws Exception {

        // First, identify what has changed.  Note that we only allow a single ETD per Project
        JsonExtractTechnicalDetails addedETD = null;
        ExtractTechnicalDetailsEntity removedETD = null;
        JsonExtractTechnicalDetails changedETD = null;

        if (oldETD == null) {
            if (updatedETD == null) {
                // No ETDs at all - nothing to do
            } else {
                addedETD = updatedETD;
            }
        } else {
            if (updatedETD == null) {
                removedETD = oldETD;
            } else {
                if (updatedETD.getUuid().equals(oldETD.getUuid())) {
                    if (oldETD.equals(updatedETD)) {
                        // No change
                    } else {
                        changedETD = updatedETD;
                    }
                } else {
                    removedETD = oldETD;
                    addedETD = updatedETD;
                }
            }
        }

        ExtractTechnicalDetailsDAL etdDAL = new ExtractTechnicalDetailsDAL();

        Short thisMapTypeId = MapType.PROJECT.getMapType();
        Short otherMapTypeId = MapType.EXTRACTTECHNICALDETAILS.getMapType();

        // Now apply changes
        if (removedETD != null) {
            etdDAL.deleteExtractTechnicalDetails(removedETD.getUuid());

            List<String> removedETDUuids = new ArrayList<>(Arrays.asList(removedETD.getUuid()));
            deleteMappings(false, thisItem, removedETDUuids, thisMapTypeId, otherMapTypeId);

            ((ObjectNode) auditJson).put(buildChangeDescription(false, false, thisMapTypeId, otherMapTypeId),
                    removedETD.toString());
        }

        if (addedETD != null) {
            if (addedETD.getUuid() == null) {
                addedETD.setUuid(UUID.randomUUID().toString());
            }

            etdDAL.saveExtractTechnicalDetails(addedETD);

            List<String> addedETDUuids = new ArrayList<>(Arrays.asList(addedETD.getUuid()));
            saveMappings(false, thisItem, addedETDUuids, thisMapTypeId, otherMapTypeId);

            ((ObjectNode) auditJson).put(buildChangeDescription(false, true, thisMapTypeId, otherMapTypeId),
                    addedETD.toString());
        }

        if (changedETD != null) {
            etdDAL.updateExtractTechnicalDetails(changedETD);

            ((ObjectNode) auditJson).put(buildChangeDescription(false, false, true, thisMapTypeId, otherMapTypeId),
                    buildBeforeAfter(oldETD.toString(), changedETD.toString()));
        }
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

    static String buildChangeDescription(boolean thisItemIsChild, boolean added, Short thisMapTypeId, Short otherMapTypeId) {
        return buildChangeDescription(thisItemIsChild, added, false, thisMapTypeId, otherMapTypeId);
    }

    static String buildChangeDescription(boolean thisItemIsChild, boolean added, boolean updated, Short thisMapTypeId, Short otherMapTypeId) {
        List<String> components = new ArrayList<>();

        if (updated) {
            components.add("Updated");
        } else {
            components.add(added ? "Added" : "Removed");
        }

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

    static String buildBeforeAfter(String before, String after) {
        return "Before: " + before + "; After: " + after;
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
}
