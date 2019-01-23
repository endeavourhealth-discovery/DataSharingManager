package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanagermodel.models.database.DocumentationEntity;
import org.endeavourhealth.datasharingmanagermodel.models.database.MasterMappingEntity;
import org.endeavourhealth.datasharingmanagermodel.models.enums.MapType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

@Path("/documentation")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to associated Documentation")
public final class DocumentEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DocumentEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return a document based on UUID")
    public Response getDocument(@Context SecurityContext sc,
                                @ApiParam(value = "UUID of document to return") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Documents(s)",
                "Document Id", uuid);

        return getSingleDocument(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DocumentEndpoint.GetAssociatedDocuments")
    @Path("/associated")
    @ApiOperation(value = "Return all associated documentation for an entity.  Takes the UUID of the entity " +
            "and the type of the entity")
    public Response getAssociatedDocuments(@Context SecurityContext sc,
                                           @ApiParam(value = "UUID of entity") @QueryParam("parentUuid") String parentUuid,
                                           @ApiParam(value = "Type of entity") @QueryParam("parentType") Short parentType
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Documents(s)",
                "Parent Id", parentUuid,
                "Parent Type", parentType);

        return getAssociatedDocuments(parentUuid, parentType);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.CohortEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete a document based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteDocument(@Context SecurityContext sc,
                                   @ApiParam(value = "UUID of the document to be deleted") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Cohort",
                "Cohort Id", uuid);

        DocumentationEntity.deleteDocument(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    private Response getSingleDocument(String uuid) throws Exception {
        DocumentationEntity documentationEntity = DocumentationEntity.getDocument(uuid);

        return Response
                .ok()
                .entity(documentationEntity)
                .build();

    }

    private Response getAssociatedDocuments(String parentUuid, Short parentType) throws Exception {

        List<String> documentUuids = MasterMappingEntity.getChildMappings(parentUuid, parentType, MapType.DOCUMENT.getMapType());
        List<DocumentationEntity> ret = new ArrayList<>();

        if (!documentUuids.isEmpty())
            ret = DocumentationEntity.getDocumentsFromList(documentUuids);

        return Response
                .ok()
                .entity(ret)
                .build();

    }



}
