package org.endeavourhealth.datasharingmanager.api.DAL;

import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DocumentationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDocumentation;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

public class DocumentationDAL {

    public DocumentationEntity getDocument(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DocumentationEntity ret = entityManager.find(DocumentationEntity.class, uuid);
        entityManager.close();

        return ret;
    }

    public void saveDocument(JsonDocumentation document) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DocumentationEntity documentationEntity = new DocumentationEntity();
        documentationEntity.setUuid(document.getUuid());
        documentationEntity.setFilename(document.getFilename());
        documentationEntity.setTitle(document.getTitle());
        documentationEntity.setFileData(document.getFileData());
        entityManager.getTransaction().begin();
        entityManager.persist(documentationEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void updateDocument(JsonDocumentation document) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DocumentationEntity documentationEntity = entityManager.find(DocumentationEntity.class, document.getUuid());
        entityManager.getTransaction().begin();
        documentationEntity.setTitle(document.getTitle());
        documentationEntity.setFilename(document.getFilename());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void deleteDocument(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        DocumentationEntity documentationEntity = entityManager.find(DocumentationEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(documentationEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void deleteAllAssociatedDocuments(String parentUuid, Short parentMapType) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();
        List<String> documents = new SecurityMasterMappingDAL().getChildMappings(parentUuid, parentMapType, MapType.DOCUMENT.getMapType());

        if (documents.size() == 0)
            return;

        entityManager.getTransaction().begin();
        CriteriaBuilder criteriaBuilder  = entityManager.getCriteriaBuilder();
        CriteriaDelete<DocumentationEntity> query = criteriaBuilder.createCriteriaDelete(DocumentationEntity.class);
        Root<DocumentationEntity> root = query.from(DocumentationEntity.class);
        query.where(root.get("uuid").in(documents));

        entityManager.createQuery(query).executeUpdate();
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public List<DocumentationEntity> getDocumentsFromList(List<String> documents) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DocumentationEntity> cq = cb.createQuery(DocumentationEntity.class);
        Root<DocumentationEntity> rootEntry = cq.from(DocumentationEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(documents);

        cq.where(predicate);
        TypedQuery<DocumentationEntity> query = entityManager.createQuery(cq);

        List<DocumentationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
