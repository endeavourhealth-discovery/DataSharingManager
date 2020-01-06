package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.PurposeEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonPurpose;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.Comparator;
import java.util.List;

public class PurposeDAL {

    public void savePurpose(JsonPurpose purpose, EntityManager entityManager) throws Exception {
        PurposeEntity dsaPurpose = new PurposeEntity();
        dsaPurpose.setUuid(purpose.getUuid());
        dsaPurpose.setDetail(purpose.getDetail());
        dsaPurpose.setTitle(purpose.getTitle());
        entityManager.merge(dsaPurpose);
    }

    public List<PurposeEntity> getPurposesFromList(List<String> purposes) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<PurposeEntity> cq = cb.createQuery(PurposeEntity.class);
            Root<PurposeEntity> rootEntry = cq.from(PurposeEntity.class);

            Predicate predicate = rootEntry.get("uuid").in(purposes);

            cq.where(predicate);
            TypedQuery<PurposeEntity> query = entityManager.createQuery(cq);

            List<PurposeEntity> ret = query.getResultList();

            ret.sort(Comparator.comparing(PurposeEntity::getTitle));

            return ret;
        } finally {
            entityManager.close();
        }
    }
}
