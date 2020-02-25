package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonExtractTechnicalDetails;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ExtractTechnicalDetailsEntity;

import javax.persistence.EntityManager;

public class ExtractTechnicalDetailsDAL {

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
            extractTechnicalDetailsEntity.setPgpCustomerPublicKeyFilename(extractTechnicalDetails.getPgpCustomerPublicKeyFilename());
            extractTechnicalDetailsEntity.setPgpCustomerPublicKeyFileData(extractTechnicalDetails.getPgpCustomerPublicKeyFileData());
            extractTechnicalDetailsEntity.setPgpInternalPublicKeyFilename(extractTechnicalDetails.getPgpInternalPublicKeyFilename());
            extractTechnicalDetailsEntity.setPgpInternalPublicKeyFileData(extractTechnicalDetails.getPgpInternalPublicKeyFileData());
            extractTechnicalDetailsEntity.setOutputFormat(extractTechnicalDetails.getOutputFormat());
            extractTechnicalDetailsEntity.setSecurityInfrastructure(extractTechnicalDetails.getSecurityInfrastructure());
            extractTechnicalDetailsEntity.setSecurityArchitecture(extractTechnicalDetails.getSecurityArchitecture());
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
            extractTechnicalDetailsEntity.setPgpCustomerPublicKeyFilename(extractTechnicalDetails.getPgpCustomerPublicKeyFilename());
            extractTechnicalDetailsEntity.setPgpCustomerPublicKeyFileData(extractTechnicalDetails.getPgpCustomerPublicKeyFileData());
            extractTechnicalDetailsEntity.setPgpInternalPublicKeyFilename(extractTechnicalDetails.getPgpInternalPublicKeyFilename());
            extractTechnicalDetailsEntity.setPgpInternalPublicKeyFileData(extractTechnicalDetails.getPgpInternalPublicKeyFileData());
            extractTechnicalDetailsEntity.setOutputFormat(extractTechnicalDetails.getOutputFormat());
            extractTechnicalDetailsEntity.setSecurityInfrastructure(extractTechnicalDetails.getSecurityInfrastructure());
            extractTechnicalDetailsEntity.setSecurityArchitecture(extractTechnicalDetails.getSecurityArchitecture());
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
