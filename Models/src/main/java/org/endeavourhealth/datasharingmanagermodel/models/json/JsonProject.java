package org.endeavourhealth.datasharingmanagermodel.models.json;

import java.util.Map;
import java.util.UUID;

public class JsonProject {
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
    private Map<UUID, String> publishers = null;
    private Map<UUID, String> subscribers = null;
    private Map<UUID, String> basePopulation = null;
    private Map<UUID, String> dataSet = null;
    private Map<UUID, String> projectConfiguration = null;
    private Map<UUID, String> dsas = null;

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

    public String getLeadUser() {
        return leadUser;
    }

    public void setLeadUser(String leadUser) {
        this.leadUser = leadUser;
    }

    public String getTechnicalLeadUser() {
        return technicalLeadUser;
    }

    public void setTechnicalLeadUser(String technicalLeadUser) {
        this.technicalLeadUser = technicalLeadUser;
    }

    public Short getConsentModelId() {
        return consentModelId;
    }

    public void setConsentModelId(Short consentModelId) {
        this.consentModelId = consentModelId;
    }

    public Short getDeidentificationLevel() {
        return deidentificationLevel;
    }

    public void setDeidentificationLevel(Short deidentificationLevel) {
        this.deidentificationLevel = deidentificationLevel;
    }

    public Short getProjectTypeId() {
        return projectTypeId;
    }

    public void setProjectTypeId(Short projectTypeId) {
        this.projectTypeId = projectTypeId;
    }

    public Short getSecurityInfrastructureId() {
        return securityInfrastructureId;
    }

    public void setSecurityInfrastructureId(Short securityInfrastructureId) {
        this.securityInfrastructureId = securityInfrastructureId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBusinessCase() {
        return businessCase;
    }

    public void setBusinessCase(String businessCase) {
        this.businessCase = businessCase;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public short getSecurityArchitectureId() {
        return securityArchitectureId;
    }

    public void setSecurityArchitectureId(short securityArchitectureId) {
        this.securityArchitectureId = securityArchitectureId;
    }

    public short getStorageProtocolId() {
        return storageProtocolId;
    }

    public void setStorageProtocolId(short storageProtocolId) {
        this.storageProtocolId = storageProtocolId;
    }

    public Map<UUID, String> getPublishers() {
        return publishers;
    }

    public void setPublishers(Map<UUID, String> publishers) {
        this.publishers = publishers;
    }

    public Map<UUID, String> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Map<UUID, String> subscribers) {
        this.subscribers = subscribers;
    }

    public Map<UUID, String> getBasePopulation() {
        return basePopulation;
    }

    public void setBasePopulation(Map<UUID, String> basePopulation) {
        this.basePopulation = basePopulation;
    }

    public Map<UUID, String> getDataSet() {
        return dataSet;
    }

    public void setDataSet(Map<UUID, String> dataSet) {
        this.dataSet = dataSet;
    }

    public Map<UUID, String> getProjectConfiguration() {
        return projectConfiguration;
    }

    public void setProjectConfiguration(Map<UUID, String> projectConfiguration) {
        this.projectConfiguration = projectConfiguration;
    }

    public Map<UUID, String> getDsas() {
        return dsas;
    }

    public void setDsas(Map<UUID, String> dsas) {
        this.dsas = dsas;
    }
}
