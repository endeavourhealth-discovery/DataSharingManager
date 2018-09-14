package org.endeavourhealth.datasharingmanagermodel.models.json;

public class JsonProjectApplicationPolicy {
    private String projectUuid = null;
    private String applicationPolicyId = null;

    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public String getApplicationPolicyId() {
        return applicationPolicyId;
    }

    public void setApplicationPolicyId(String applicationPolicyId) {
        this.applicationPolicyId = applicationPolicyId;
    }
}
