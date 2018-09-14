package org.endeavourhealth.datasharingmanagermodel.models.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.endeavourhealth.datasharingmanagermodel.PersistenceManager;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonProjectApplicationPolicy;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "project_application_policy", schema = "data_sharing_manager")
public class ProjectApplicationPolicyEntity {
    private String projectUuid;
    private String applicationPolicyId;

    @Id
    @Column(name = "project_uuid")
    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    @Basic
    @Column(name = "application_policy_id")
    public String getApplicationPolicyId() {
        return applicationPolicyId;
    }

    public void setApplicationPolicyId(String applicationPolicyId) {
        this.applicationPolicyId = applicationPolicyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectApplicationPolicyEntity that = (ProjectApplicationPolicyEntity) o;
        return Objects.equals(projectUuid, that.projectUuid) &&
                Objects.equals(applicationPolicyId, that.applicationPolicyId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(projectUuid, applicationPolicyId);
    }

    public ProjectApplicationPolicyEntity() {
    }

    public ProjectApplicationPolicyEntity(JsonProjectApplicationPolicy jsonProjectApplicationPolicy) {
        this.projectUuid = jsonProjectApplicationPolicy.getProjectUuid();
        this.applicationPolicyId = jsonProjectApplicationPolicy.getApplicationPolicyId();
    }

    public static ProjectApplicationPolicyEntity getProjectApplicationPolicyId(String projectUuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        ProjectApplicationPolicyEntity ret = entityManager.find(ProjectApplicationPolicyEntity.class, projectUuid);

        entityManager.close();

        return ret;
    }

    public static void saveProjectApplicationPolicyId(JsonProjectApplicationPolicy projectApplicationPolicy) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        ProjectApplicationPolicyEntity oldPolicy = ProjectApplicationPolicyEntity.getProjectApplicationPolicyId(projectApplicationPolicy.getProjectUuid());

        if (oldPolicy != null) {
            if (oldPolicy.applicationPolicyId.equals(projectApplicationPolicy.getApplicationPolicyId())) {
                // application policy hasnt changed so don't save
                return;
            }
        }

        entityManager.getTransaction().begin();
        ProjectApplicationPolicyEntity projectApplicationPolicyEntity = new ProjectApplicationPolicyEntity();
        projectApplicationPolicyEntity.setProjectUuid(projectApplicationPolicy.getProjectUuid());
        projectApplicationPolicyEntity.setApplicationPolicyId(projectApplicationPolicy.getApplicationPolicyId());
        entityManager.merge(projectApplicationPolicyEntity);
        entityManager.getTransaction().commit();

        entityManager.close();

    }
}
