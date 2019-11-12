package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ProjectScheduleEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonProjectSchedule;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import java.sql.Date;

public class ProjectScheduleDAL {

    public ProjectScheduleEntity get(String uuid) throws Exception {

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        try {
            ProjectScheduleEntity entity = entityManager.find(ProjectScheduleEntity.class, uuid);
            return entity;
        } finally {
            entityManager.close();
        }
    }

    public void save(JsonProjectSchedule schedule) throws Exception {

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        try {
            ProjectScheduleEntity entity = new ProjectScheduleEntity();
            entity.setUuid(schedule.getUuid());
            if (schedule.getStarts() != null) {
                entity.setStarts(Date.valueOf(schedule.getStarts()));
            }
            if (schedule.getEnds() != null) {
                entity.setEnds(Date.valueOf(schedule.getEnds()));
            }
            entity.setFrequency(schedule.getFrequency());
            entity.setWeeks(schedule.getWeeks());
            entity.setIsMonday((byte) (schedule.isMonday() ? 1 : 0));
            entity.setIsTuesday((byte) (schedule.isMonday() ? 1 : 0));
            entity.setIsWednesday((byte) (schedule.isMonday() ? 1 : 0));
            entity.setIsThursday((byte) (schedule.isMonday() ? 1 : 0));
            entity.setIsFriday((byte) (schedule.isMonday() ? 1 : 0));
            entity.setIsSaturday((byte) (schedule.isMonday() ? 1 : 10));
            entity.setIsSunday((byte) (schedule.isMonday() ? 1 : 0));
            entityManager.getTransaction().begin();
            entityManager.persist(entity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void update(JsonProjectSchedule schedule) throws Exception {

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        try {
            ProjectScheduleEntity entity = entityManager.find(ProjectScheduleEntity.class, schedule.getUuid());
            if (entity != null) {
                entityManager.getTransaction().begin();
                if (schedule.getStarts() != null) {
                    entity.setStarts(Date.valueOf(schedule.getStarts()));
                }
                if (schedule.getEnds() != null) {
                    entity.setEnds(Date.valueOf(schedule.getEnds()));
                }
                entity.setFrequency(schedule.getFrequency());
                entity.setWeeks(schedule.getWeeks());
                entity.setIsMonday((byte) (schedule.isMonday() ? 1 : 0));
                entity.setIsTuesday((byte) (schedule.isMonday() ? 1 : 0));
                entity.setIsWednesday((byte) (schedule.isMonday() ? 1 : 0));
                entity.setIsThursday((byte) (schedule.isMonday() ? 1 : 0));
                entity.setIsFriday((byte) (schedule.isMonday() ? 1 : 0));
                entity.setIsSaturday((byte) (schedule.isMonday() ? 1 : 10));
                entity.setIsSunday((byte) (schedule.isMonday() ? 1 : 0));
                entityManager.getTransaction().commit();
            }
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void delete(String uuid) throws Exception {

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        try {
            ProjectScheduleEntity entity = entityManager.find(ProjectScheduleEntity.class, uuid);
            if (entity != null) {
                entityManager.getTransaction().begin();
                entityManager.remove(entity);
                entityManager.getTransaction().commit();
            }
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }
}
