package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.OrganisationTypeEntity;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class OrganisationTypeDAL {

    public List<OrganisationTypeEntity> getAllOrganisationTypes() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<OrganisationTypeEntity> cq = cb.createQuery(OrganisationTypeEntity.class);
            Root<OrganisationTypeEntity> rootEntry = cq.from(OrganisationTypeEntity.class);
            CriteriaQuery<OrganisationTypeEntity> all = cq.select(rootEntry);
            TypedQuery<OrganisationTypeEntity> allQuery = entityManager.createQuery(all);
            List<OrganisationTypeEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }

    }
}
