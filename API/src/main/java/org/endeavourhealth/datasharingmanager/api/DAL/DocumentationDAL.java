package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DocumentationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDocumentation;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.DocumentationCache;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

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
