package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DatasetEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDataSet;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class DatasetDAL {

    public List<DatasetEntity> getAllDataSets() throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DatasetEntity> cq = cb.createQuery(DatasetEntity.class);
            Root<DatasetEntity> rootEntry = cq.from(DatasetEntity.class);
            CriteriaQuery<DatasetEntity> all = cq.select(rootEntry);
            TypedQuery<DatasetEntity> allQuery = entityManager.createQuery(all);

            List<DatasetEntity> ret = allQuery.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }

    }

    public DatasetEntity getDataSet(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DatasetEntity ret = entityManager.find(DatasetEntity.class, uuid);

            return ret;
        } finally {
            entityManager.close();
        }

    }

    public void updateDataSet(JsonDataSet dataset) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DatasetEntity dataSetEntity = entityManager.find(DatasetEntity.class, dataset.getUuid());
            entityManager.getTransaction().begin();
            dataSetEntity.setName(dataset.getName());
            dataSetEntity.setDescription(dataset.getDescription());
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void saveDataSet(JsonDataSet dataset) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DatasetEntity dataSetEntity = new DatasetEntity();
            entityManager.getTransaction().begin();
            dataSetEntity.setUuid(dataset.getUuid());
            dataSetEntity.setName(dataset.getName());
            dataSetEntity.setDescription(dataset.getDescription());
            entityManager.persist(dataSetEntity);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void deleteDataSet(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            DatasetEntity dataSetEntity = entityManager.find(DatasetEntity.class, uuid);
            entityManager.getTransaction().begin();
            entityManager.remove(dataSetEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public List<DatasetEntity> search(String expression) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DatasetEntity> cq = cb.createQuery(DatasetEntity.class);
            Root<DatasetEntity> rootEntry = cq.from(DatasetEntity.class);

            Predicate predicate = cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%");

            cq.where(predicate);
            TypedQuery<DatasetEntity> query = entityManager.createQuery(cq);
            List<DatasetEntity> ret = query.getResultList();

            return ret;
        } finally {
            entityManager.close();
        }

    }
}
