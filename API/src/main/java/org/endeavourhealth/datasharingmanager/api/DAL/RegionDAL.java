package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.RegionEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonRegion;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class RegionDAL {

    public void updateRegion(JsonRegion region) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        RegionEntity re = entityManager.find(RegionEntity.class, region.getUuid());
        entityManager.getTransaction().begin();
        re.setDescription(region.getDescription());
        re.setName(region.getName());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void saveRegion(JsonRegion region) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        RegionEntity re = new RegionEntity();
        entityManager.getTransaction().begin();
        re.setDescription(region.getDescription());
        re.setName(region.getName());
        re.setUuid(region.getUuid());
        entityManager.persist(re);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void deleteRegion(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        RegionEntity re = entityManager.find(RegionEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(re);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public List<RegionEntity> getRegionsFromList(List<String> regions) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RegionEntity> cq = cb.createQuery(RegionEntity.class);
        Root<RegionEntity> rootEntry = cq.from(RegionEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(regions);

        cq.where(predicate);
        TypedQuery<RegionEntity> query = entityManager.createQuery(cq);

        List<RegionEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public List<RegionEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RegionEntity> cq = cb.createQuery(RegionEntity.class);
        Root<RegionEntity> rootEntry = cq.from(RegionEntity.class);

        Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

        cq.where(predicate);
        TypedQuery<RegionEntity> query = entityManager.createQuery(cq);
        List<RegionEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
