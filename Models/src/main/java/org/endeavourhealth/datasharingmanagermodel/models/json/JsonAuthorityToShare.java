package org.endeavourhealth.datasharingmanagermodel.models.json;

import org.endeavourhealth.usermanagermodel.models.json.JsonUser;

import java.util.ArrayList;
import java.util.List;

public class JsonAuthorityToShare {
    private String organisationId = null;
    private String organisationName = null;
    private String organisationOdsCode = null;
    private List<JsonUser> users = new ArrayList<>();

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getOrganisationOdsCode() {
        return organisationOdsCode;
    }

    public void setOrganisationOdsCode(String organisationOdsCode) {
        this.organisationOdsCode = organisationOdsCode;
    }

    public List<JsonUser> getUsers() {
        return users;
    }

    public void setUsers(List<JsonUser> users) {
        this.users = users;
    }

    public void addUser(JsonUser user) {
        if (this.users == null) {
            this.users = new ArrayList();
        }

        this.users.add(user);
    }
}
