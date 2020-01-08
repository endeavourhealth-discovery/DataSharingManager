package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectApplicationPolicyDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ProjectApplicationPolicyEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonProjectApplicationPolicy;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;

public class ProjectApplicationPolicyDAL {

    public void saveProjectApplicationPolicyId(JsonProjectApplicationPolicy projectApplicationPolicy) throws Exception {

        ProjectApplicationPolicyEntity oldPolicy = new SecurityProjectApplicationPolicyDAL().getProjectApplicationPolicyId(projectApplicationPolicy.getProjectUuid());

        if (oldPolicy != null) {
            if (oldPolicy.getApplicationPolicyId().equals(projectApplicationPolicy.getApplicationPolicyId())) {
                // application policy hasnt changed so don't save
                return;
            }
        }

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            //TODO: Audit this? Or, move Application Policy to a field in Project, or to MasterMapping?
            entityManager.getTransaction().begin();
            ProjectApplicationPolicyEntity projectApplicationPolicyEntity = new ProjectApplicationPolicyEntity();
            projectApplicationPolicyEntity.setProjectUuid(projectApplicationPolicy.getProjectUuid());
            projectApplicationPolicyEntity.setApplicationPolicyId(projectApplicationPolicy.getApplicationPolicyId());
            entityManager.merge(projectApplicationPolicyEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }

    }
}
