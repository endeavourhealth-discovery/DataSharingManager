package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.ProjectApplicationPolicyDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonProjectApplicationPolicy;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ProjectApplicationPolicyEntity;

import javax.persistence.EntityManager;

public class ProjectApplicationPolicyDAL {
    private static ProjectApplicationPolicyDalI projectApplicationRepository = DalProvider.factoryDSMProjectApplicationPolicyDal();

    public void saveProjectApplicationPolicyId(JsonProjectApplicationPolicy projectApplicationPolicy) throws Exception {

        ProjectApplicationPolicyEntity oldPolicy = projectApplicationRepository.getProjectApplicationPolicyId(projectApplicationPolicy.getProjectUuid());

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
