package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ExtractTechnicalDetailsEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonExtractTechnicalDetails;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;

public class ExtractTechnicalDetailsDAL {

    public ExtractTechnicalDetailsEntity getExtractTechnicalDetails(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            ExtractTechnicalDetailsEntity ret = entityManager.find(ExtractTechnicalDetailsEntity.class, uuid);

            return ret;
        } finally {
            entityManager.close();
        }

    }

    public void saveExtractTechnicalDetails(JsonExtractTechnicalDetails extractTechnicalDetails) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            ExtractTechnicalDetailsEntity extractTechnicalDetailsEntity = new ExtractTechnicalDetailsEntity();
            extractTechnicalDetailsEntity.setUuid(extractTechnicalDetails.getUuid());
            extractTechnicalDetailsEntity.setName(extractTechnicalDetails.getName());
            extractTechnicalDetailsEntity.setSftpHostName(extractTechnicalDetails.getSftpHostName());
            extractTechnicalDetailsEntity.setSftpHostDirectory(extractTechnicalDetails.getSftpHostDirectory());
            extractTechnicalDetailsEntity.setSftpHostPort(extractTechnicalDetails.getSftpHostPort());
            extractTechnicalDetailsEntity.setSftpClientUsername(extractTechnicalDetails.getSftpClientUsername());
            extractTechnicalDetailsEntity.setSftpClientPrivateKeyPassword(extractTechnicalDetails.getSftpClientPrivateKeyPassword());
            extractTechnicalDetailsEntity.setSftpHostPublicKeyFilename(extractTechnicalDetails.getSftpHostPublicKeyFilename());
            extractTechnicalDetailsEntity.setSftpHostPublicKeyFileData(extractTechnicalDetails.getSftpHostPublicKeyFileData());
            extractTechnicalDetailsEntity.setSftpClientPrivateKeyFilename(extractTechnicalDetails.getSftpClientPrivateKeyFilename());
            extractTechnicalDetailsEntity.setSftpClientPrivateKeyFileData(extractTechnicalDetails.getSftpClientPrivateKeyFileData());
            entityManager.getTransaction().begin();
            entityManager.persist(extractTechnicalDetailsEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void updateExtractTechnicalDetails(JsonExtractTechnicalDetails extractTechnicalDetails) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            ExtractTechnicalDetailsEntity extractTechnicalDetailsEntity = entityManager.find(ExtractTechnicalDetailsEntity.class, extractTechnicalDetails.getUuid());
            entityManager.getTransaction().begin();
            extractTechnicalDetailsEntity.setName(extractTechnicalDetails.getName());
            extractTechnicalDetailsEntity.setSftpHostName(extractTechnicalDetails.getSftpHostName());
            extractTechnicalDetailsEntity.setSftpHostDirectory(extractTechnicalDetails.getSftpHostDirectory());
            extractTechnicalDetailsEntity.setSftpHostPort(extractTechnicalDetails.getSftpHostPort());
            extractTechnicalDetailsEntity.setSftpClientUsername(extractTechnicalDetails.getSftpClientUsername());
            extractTechnicalDetailsEntity.setSftpClientPrivateKeyPassword(extractTechnicalDetails.getSftpClientPrivateKeyPassword());
            extractTechnicalDetailsEntity.setSftpHostPublicKeyFilename(extractTechnicalDetails.getSftpHostPublicKeyFilename());
            extractTechnicalDetailsEntity.setSftpHostPublicKeyFileData(extractTechnicalDetails.getSftpHostPublicKeyFileData());
            extractTechnicalDetailsEntity.setSftpClientPrivateKeyFilename(extractTechnicalDetails.getSftpClientPrivateKeyFilename());
            extractTechnicalDetailsEntity.setSftpClientPrivateKeyFileData(extractTechnicalDetails.getSftpClientPrivateKeyFileData());
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void deleteExtractTechnicalDetails(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            ExtractTechnicalDetailsEntity extractTechnicalDetailsEntity = entityManager.find(ExtractTechnicalDetailsEntity.class, uuid);
            entityManager.getTransaction().begin();
            entityManager.remove(extractTechnicalDetailsEntity);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

}
