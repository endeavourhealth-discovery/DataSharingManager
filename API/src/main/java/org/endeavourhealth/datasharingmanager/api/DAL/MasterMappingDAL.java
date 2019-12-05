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
import java.util.*;
import java.util.concurrent.CompletableFuture;

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

        String test = MapType.valueOfTypeId((short)55);
        // DPAs
        auditJson = updateMappingsAndGetAudit(true, updatedDataSet.getUuid(), oldDataset.getDpas(),
                updatedDataSet.getDpas(), MapType.DATASET.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson, entityManager);

        return auditJson;
    }

    public JsonNode updateCohortMappings(JsonCohort updatedCohort, CohortEntity oldCohort, JsonNode auditJson, EntityManager entityManager) throws Exception {

        // DPAs
        auditJson = updateMappingsAndGetAudit(true, updatedCohort.getUuid(), oldCohort.getDpas(),
                updatedCohort.getDpas(), MapType.COHORT.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType(), auditJson, entityManager);

        return auditJson;

    }

    private JsonNode appendToJson(boolean added, List<String> mappings, String type, JsonNode auditJson) throws Exception {
        if (!mappings.isEmpty()) {
            return new AuditCompareLogic().generateListDifferenceAuditJson(auditJson, added, mappings, type);
        }

        return auditJson;
    }

    private void updateMappings(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                List<String> updatedMappings, Short thisMapTypeId, Short otherMapTypeId,
                                List<String> removedMappings, List<String> addedMappings, EntityManager entityManager) throws Exception {

        for (String oldMapping : oldMappings) {
            if (!updatedMappings.contains(oldMapping)) {
                removedMappings.add(oldMapping);
            }
        }

        for (String updatedMapping : updatedMappings) {
            if (!oldMappings.contains(updatedMapping)) {
                addedMappings.add(updatedMapping);
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
                // entityManager.getTransaction().rollback();
                throw e;
            } /*finally {
                entityManager.close();
            }*/
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

    public void saveRegionMappings(JsonRegion region) throws Exception {

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
                    new AddressDAL().getGeoLocationsForOrganisations(new ArrayList<>(organisations.keySet()));
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

    public void saveDataSharingAgreementMappings(JsonDSA dsa) throws Exception {

        if (dsa.getDataFlows() != null) {
            Map<UUID, String> dataFlows = dsa.getDataFlows();
            saveChildMappings(dataFlows, MapType.DATAFLOW.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }

        if (dsa.getRegions() != null) {
            Map<UUID, String> regions = dsa.getRegions();
            saveParentMappings(regions, MapType.REGION.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }

        if (dsa.getProjects() != null) {
            Map<UUID, String> projects = dsa.getProjects();
            saveChildMappings(projects, MapType.PROJECT.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }

        if (dsa.getPublishers() != null) {
            Map<UUID, String> publishers = dsa.getPublishers();
            saveChildMappings(publishers, MapType.PUBLISHER.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());

            CompletableFuture.runAsync(() -> {
                try {
                    new AddressDAL().getGeoLocationsForOrganisations(new ArrayList<>(publishers.keySet()));
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
                    new AddressDAL().getGeoLocationsForOrganisations(new ArrayList<>(subscribers.keySet()));
                } catch (Exception e) {
                    // ignore error;
                }
            });
        }

        if (dsa.getDocumentations() != null) {
            Map<UUID, String> documentation = new HashMap<>();
            List<JsonDocumentation> jsonDocumentations = dsa.getDocumentations();
            for (JsonDocumentation doc : jsonDocumentations) {
                documentation.put(UUID.fromString(doc.getUuid()), doc.getTitle());
            }
            saveChildMappings(documentation, MapType.DOCUMENT.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }

        if (dsa.getPurposes() != null) {
            Map<UUID, String> purposes = new HashMap<>();
            List<JsonPurpose> jsonPurposes = dsa.getPurposes();
            for (JsonPurpose purp : jsonPurposes) {
                purposes.put(UUID.fromString(purp.getUuid()), purp.getTitle());
            }
            saveChildMappings(purposes, MapType.PURPOSE.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }

        if (dsa.getBenefits() != null) {
            Map<UUID, String> benefits = new HashMap<>();
            List<JsonPurpose> jsonBenefits = dsa.getBenefits();
            for (JsonPurpose benef : jsonBenefits) {
                benefits.put(UUID.fromString(benef.getUuid()), benef.getTitle());
            }
            saveChildMappings(benefits, MapType.BENEFIT.getMapType(), dsa.getUuid(), MapType.DATASHARINGAGREEMENT.getMapType());
        }
    }

    public void saveDataFlowMappings(JsonDataFlow dataFlow) throws Exception {

        if (dataFlow.getDsas() != null) {
            Map<UUID, String> dsas = dataFlow.getDsas();
            saveParentMappings(dsas, MapType.DATASHARINGAGREEMENT.getMapType(), dataFlow.getUuid(), MapType.DATAFLOW.getMapType());
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
            List<JsonDocumentation> jsonDocumentations = dataFlow.getDocumentations();
            for (JsonDocumentation doc : jsonDocumentations) {
                documentation.put(UUID.fromString(doc.getUuid()), doc.getTitle());
            }
            saveChildMappings(documentation, MapType.DOCUMENT.getMapType(), dataFlow.getUuid(), MapType.DATAFLOW.getMapType());
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

    private JsonNode updateMappingsAndGetAudit(boolean thisItemIsChild, String thisItem, List<String> oldMappings,
                                               Map<UUID, String> updatedMap, Short thisMapTypeId, Short otherMapTypeId,
                                               JsonNode auditJson, EntityManager entityManager) throws Exception {

        List<String> updatedMappings = new ArrayList<String>();
        List<String> removedMappings = new ArrayList<>();
        List<String> addedMappings = new ArrayList<>();
        updatedMap.forEach((k, v) -> updatedMappings.add(k.toString()));

        updateMappings(thisItemIsChild, thisItem, oldMappings, updatedMappings,
                thisMapTypeId, otherMapTypeId, removedMappings, addedMappings, entityManager);

        auditJson = appendToJson(false, removedMappings, MapType.valueOfTypeId(otherMapTypeId), auditJson);
        auditJson = appendToJson(true, addedMappings, MapType.valueOfTypeId(otherMapTypeId), auditJson);

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

        auditJson = appendToJson(false, removedMappings, MapType.valueOfTypeId(documentMapType), auditJson);
        auditJson = appendToJson(true, addedMappings, MapType.valueOfTypeId(documentMapType), auditJson);

        return auditJson;
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

    private JsonNode updateMappingsAndGetAuditForExtractTechnicalDetails(String parentItem, ExtractTechnicalDetailsEntity oldExtractTechDetails,
                                                                         JsonExtractTechnicalDetails newExtractTechDetails, Short thisMapTypeId,
                                                                         JsonNode auditJson, EntityManager entityManager) throws Exception {

        List<String> updatedMappings = new ArrayList<>();
        updatedMappings.add(newExtractTechDetails.getUuid());
        List<String> oldExtractTechDetailsList = new ArrayList<>();
        oldExtractTechDetailsList.add(oldExtractTechDetails.getUuid());
        List<JsonExtractTechnicalDetails> newExtractTechDetailsList = new ArrayList<>();
        newExtractTechDetailsList.add(newExtractTechDetails);

        List<String> removedMappings = new ArrayList<>();
        List<String> addedMappings = new ArrayList<>();

        Short extractTechDetailsMapType = MapType.EXTRACTTECHNICALDETAILS.getMapType();

        //TODO Get this to work
        updateMappings(false, parentItem, oldExtractTechDetailsList, updatedMappings,
                thisMapTypeId, extractTechDetailsMapType, removedMappings, addedMappings, entityManager);

        deleteExtractTechnicalDetails(removedMappings);
        saveExtractTechnicalDetails(addedMappings, newExtractTechDetailsList);

        auditJson = appendToJson(false, removedMappings, MapType.valueOfTypeId(extractTechDetailsMapType), auditJson);
        auditJson = appendToJson(true, addedMappings, MapType.valueOfTypeId(extractTechDetailsMapType), auditJson);

        return auditJson;
    }

    private void deleteExtractTechnicalDetails(List<String> deletedExtractTechDetails) throws Exception {
        for (String uuid : deletedExtractTechDetails) {
            new ExtractTechnicalDetailsDAL().deleteExtractTechnicalDetails(uuid);
        }
    }

    private void saveExtractTechnicalDetails(List<String> addedExtractTechDetails,
                                             List<JsonExtractTechnicalDetails> extractTechDetails) throws Exception {
        for (JsonExtractTechnicalDetails details : extractTechDetails) {
            if (addedExtractTechDetails.contains(details.getUuid())) {
                if (details.getUuid() != null) {
                    new ExtractTechnicalDetailsDAL().updateExtractTechnicalDetails(details);
                } else {
                    details.setUuid(UUID.randomUUID().toString());
                    new ExtractTechnicalDetailsDAL().saveExtractTechnicalDetails(details);
                }
            }
        }
    }

    public void saveProjectMappings(JsonProject project) throws Exception {

        /*if (project.getPublishers() != null) {
            Map<UUID, String> publishers = project.getPublishers();
            saveChildMappings(publishers, MapType.PUBLISHER.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }

        if (project.getSubscribers() != null) {
            Map<UUID, String> subscribers = project.getSubscribers();
            saveChildMappings(subscribers, MapType.SUBSCRIBER.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }

        if (project.getCohorts() != null) {
            Map<UUID, String> dsas = project.getCohorts();
            saveChildMappings(dsas, MapType.COHORT.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }

        if (project.getDataSets() != null) {
            Map<UUID, String> dpas = project.getDataSets();
            saveChildMappings(dpas, MapType.DATASET.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }

        if (project.getDsas() != null) {
            Map<UUID, String> exchanges = project.getDsas();
            saveParentMappings(exchanges, MapType.DATASHARINGAGREEMENT.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }

        if (project.getDocumentations() != null) {
            Map<UUID, String> documentation = new HashMap<>();
            List<JsonDocumentation> jsonDocumentations = project.getDocumentations();
            for (JsonDocumentation doc : jsonDocumentations) {
                documentation.put(UUID.fromString(doc.getUuid()), doc.getTitle());
            }
            saveChildMappings(documentation, MapType.DOCUMENT.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }*/

        //TODO Remove later
        if (project.getExtractTechnicalDetails() != null) {

            Map<UUID, String> details = new HashMap<>();
            details.put(UUID.fromString(project.getExtractTechnicalDetails().getUuid()),
                    project.getExtractTechnicalDetails().getName());
            saveChildMappings(details, MapType.EXTRACTTECHNICALDETAILS.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
        }

        /*
        if (project.getSchedule() != null) {
            List<String> schedUUIDs = new SecurityMasterMappingDAL().getChildMappings(
                    project.getUuid(), MapType.PROJECT.getMapType(), MapType.SCHEDULE.getMapType());
            if (schedUUIDs.size() == 0) {
                Map<UUID, String> schedule = new HashMap<>();
                String uuid = project.getSchedule().getUuid();
                schedule.put(UUID.fromString(uuid), uuid);
                saveChildMappings(schedule, MapType.SCHEDULE.getMapType(), project.getUuid(), MapType.PROJECT.getMapType());
            }
        }
        */
    }

    public void saveDataExchangeMappings(JsonDataExchange dataExchange) throws Exception {

        if (dataExchange.getDataFlows() != null) {
            Map<UUID, String> dataFlows = dataExchange.getDataFlows();
            saveParentMappings(dataFlows, MapType.DATAFLOW.getMapType(), dataExchange.getUuid(), MapType.DATAEXCHANGE.getMapType());
        }
    }

    public void saveDataProcessingAgreementMappings(JsonDPA dpa) throws Exception {

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
            List<JsonDocumentation> jsonDocumentations = dpa.getDocumentations();
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
                    new AddressDAL().getGeoLocationsForOrganisations(new ArrayList<>(publishers.keySet()));
                } catch (Exception e) {
                    // ignore error;
                }
            });
        }

        if (dpa.getPurposes() != null) {
            Map<UUID, String> purposes = new HashMap<>();
            List<JsonPurpose> jsonPurposes = dpa.getPurposes();
            for (JsonPurpose purp : jsonPurposes) {
                purposes.put(UUID.fromString(purp.getUuid()), purp.getTitle());
            }
            saveChildMappings(purposes, MapType.PURPOSE.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        }

        if (dpa.getBenefits() != null) {
            Map<UUID, String> benefits = new HashMap<>();
            List<JsonPurpose> jsonBenefits = dpa.getBenefits();
            for (JsonPurpose benef : jsonBenefits) {
                benefits.put(UUID.fromString(benef.getUuid()), benef.getTitle());
            }
            saveChildMappings(benefits, MapType.BENEFIT.getMapType(), dpa.getUuid(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
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
        if (scheduleJson != null) {
            //No existing schedule
            if (scheduleEntity == null) {
                dal.save(scheduleJson);
                Map<UUID, String> children = new HashMap<>();
                String uuid = scheduleJson.getUuid();
                children.put(UUID.fromString(uuid), uuid);
                saveChildMappings(children, MapType.SCHEDULE.getMapType(), projectUuid, MapType.PROJECT.getMapType());
                ((ObjectNode)auditJson).put("AddedSCHEDULE",
                        StringUtils.join(scheduleJson.getCronDescription() + " ("+scheduleJson.getUuid()+")",
                                System.getProperty("line.separator")));
            } else {
                //Schedule was just updated, no need to do anything with the mappings
                if (scheduleEntity.getUuid().equals(scheduleJson.getUuid())) {
                    dal.update(scheduleJson);
                } else {
                    //Previous schedule was deleted then a new one was added
                    dal.delete(scheduleEntity.getUuid());
                    List<String> parent = new ArrayList<>();
                    parent.add(projectUuid);
                    deleteMappings(true, scheduleEntity.getUuid(), parent, MapType.SCHEDULE.getMapType(),
                            thisMapTypeId, entityManager);
                    ((ObjectNode)auditJson).put("RemovedSCHEDULE",
                            StringUtils.join(scheduleEntity.getCronDescription() + " ("+scheduleEntity.getUuid()+")",
                                    System.getProperty("line.separator")));

                    dal.save(scheduleJson);
                    Map<UUID, String> children = new HashMap<>();
                    String uuid = scheduleJson.getUuid();
                    children.put(UUID.fromString(uuid), uuid);
                    saveChildMappings(children, MapType.SCHEDULE.getMapType(), projectUuid, MapType.PROJECT.getMapType());
                    ((ObjectNode)auditJson).put("AddedSCHEDULE",
                            StringUtils.join(scheduleJson.getCronDescription() + " ("+scheduleJson.getUuid()+")",
                                    System.getProperty("line.separator")));
                }
            }
        } else if (scheduleEntity != null) {
            //schedule was deleted
            dal.delete(scheduleEntity.getUuid());
            List<String> parent = new ArrayList<>();
            parent.add(projectUuid);
            deleteMappings(true, scheduleEntity.getUuid(), parent, MapType.SCHEDULE.getMapType(),
                    thisMapTypeId, entityManager);
            ((ObjectNode)auditJson).put("RemovedSCHEDULE",
                    StringUtils.join(scheduleEntity.getCronDescription() + " ("+scheduleEntity.getUuid()+")",
                            System.getProperty("line.separator")));
        }
        return auditJson;
    }
}
