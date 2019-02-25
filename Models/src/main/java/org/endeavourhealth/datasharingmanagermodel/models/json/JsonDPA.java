package org.endeavourhealth.datasharingmanagermodel.models.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonDPA {
    private String uuid = null;
    private String name = null;
    private String description = null;
    private String derivation = null;
    private String publisherInformation = null;
    private String publisherContractInformation = null;
    private String publisherDataset = null;
    private Short dsaStatusId = null;
    private String dataFlow = null;
    private String returnToSenderPolicy = null;
    private String startDate = null;
    private String endDate = null;
    private Map<UUID, String> dataFlows = null;
    private Map<UUID, String> cohorts = null;
    private Map<UUID, String> dataSets = null;
    private Map<UUID, String> regions = null;
    private List<JsonDocumentation> documentations = new ArrayList<>();
    private Map<UUID, String> publishers = null;
    private List<JsonPurpose> purposes = new ArrayList<>();
    private List<JsonPurpose> benefits = new ArrayList<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDerivation() {
        return derivation;
    }

    public void setDerivation(String derivation) {
        this.derivation = derivation;
    }

    public String getPublisherInformation() {
        return publisherInformation;
    }

    public void setPublisherInformation(String publisherInformation) {
        this.publisherInformation = publisherInformation;
    }

    public String getPublisherContractInformation() {
        return publisherContractInformation;
    }

    public void setPublisherContractInformation(String publisherContractInformation) {
        this.publisherContractInformation = publisherContractInformation;
    }

    public String getPublisherDataset() {
        return publisherDataset;
    }

    public void setPublisherDataset(String publisherDataset) {
        this.publisherDataset = publisherDataset;
    }

    public Short getDsaStatusId() {
        return dsaStatusId;
    }

    public void setDsaStatusId(Short dsaStatusId) {
        this.dsaStatusId = dsaStatusId;
    }

    public String getDataFlow() {
        return dataFlow;
    }

    public void setDataFlow(String dataFlow) {
        this.dataFlow = dataFlow;
    }

    public String getReturnToSenderPolicy() {
        return returnToSenderPolicy;
    }

    public void setReturnToSenderPolicy(String returnToSenderPolicy) {
        this.returnToSenderPolicy = returnToSenderPolicy;
    }

    public Map<UUID, String> getDataFlows() {
        return dataFlows;
    }

    public void setDataFlows(Map<UUID, String> dataFlows) {
        this.dataFlows = dataFlows;
    }

    public Map<UUID, String> getCohorts() {
        return cohorts;
    }

    public void setCohorts(Map<UUID, String> cohorts) {
        this.cohorts = cohorts;
    }

    public Map<UUID, String> getDataSets() {
        return dataSets;
    }

    public void setDataSets(Map<UUID, String> datasets) {
        this.dataSets = datasets;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<JsonDocumentation> getDocumentations() {
        return documentations;
    }

    public void setDocumentations(List<JsonDocumentation> documentations) {
        this.documentations = documentations;
    }

    public Map<UUID, String> getPublishers() {
        return publishers;
    }

    public void setPublishers(Map<UUID, String> publishers) {
        this.publishers = publishers;
    }

    public List<JsonPurpose> getPurposes() {
        return purposes;
    }

    public void setPurposes(List<JsonPurpose> purposes) {
        this.purposes = purposes;
    }

    public List<JsonPurpose> getBenefits() {
        return benefits;
    }

    public void setBenefits(List<JsonPurpose> benefits) {
        this.benefits = benefits;
    }

    public Map<UUID, String> getRegions() {
        return regions;
    }

    public void setRegions(Map<UUID, String> regions) {
        this.regions = regions;
    }
}
