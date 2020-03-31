package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDocumentation;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DocumentationEntity;

import javax.persistence.EntityManager;

public class DocumentationDAL {

    private EntityManager _entityManager;

    public DocumentationDAL(EntityManager entityManager) {
        _entityManager = entityManager;
    }


    public void saveDocument(JsonDocumentation document) throws Exception {
        // Hack - use a different transaction here, so that the file name can be found by EntityNameGetter.replaceUUIDsWithName
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        try {
            entityManager.getTransaction().begin();

            DocumentationEntity documentationEntity = new DocumentationEntity();
            documentationEntity.setUuid(document.getUuid());
            documentationEntity.setFilename(document.getFilename());
            documentationEntity.setTitle(document.getTitle());
            documentationEntity.setFileData(document.getFileData());
            entityManager.persist(documentationEntity);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public void deleteDocument(String uuid) throws Exception {
        DocumentationEntity documentationEntity = _entityManager.find(DocumentationEntity.class, uuid);
        _entityManager.remove(documentationEntity);
    }

    public DocumentationEntity getDocument(String uuid) throws Exception {
        DocumentationEntity documentationEntity = _entityManager.find(DocumentationEntity.class, uuid);
        return documentationEntity;
    }
}
