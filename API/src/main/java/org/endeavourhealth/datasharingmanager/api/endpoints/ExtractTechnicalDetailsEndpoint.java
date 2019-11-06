package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ExtractTechnicalDetailsEntity;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.DAL.ExtractTechnicalDetailsDAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

@Path("/extractTechnicalDetails")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to associated Extract Technical Details")
public final class ExtractTechnicalDetailsEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ExtractTechnicalDetailsEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ExtractTechnicalDetailsEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return Extract Technical Details based on UUID")
    public Response getExtractTechnicalDetails(@Context SecurityContext sc,
                                @ApiParam(value = "UUID of Extract Technical Details to return") @QueryParam("uuid") String uuid
    ) throws Exception {

        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Extract Technical Details",
                "Extract Technical Details Id", uuid);

        ExtractTechnicalDetailsEntity extractTechnicalDetailsEntity = new ExtractTechnicalDetailsDAL().getExtractTechnicalDetails(uuid);

        return Response
                .ok()
                .entity(extractTechnicalDetailsEntity)
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ExtractTechnicalDetailsEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete Extract Technical Details based on UUID that is passed to the API. Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteExtractTechnicalDetails(@Context SecurityContext sc,
                                   @ApiParam(value = "UUID of Extract Technical Details to be deleted") @QueryParam("uuid") String uuid
    ) throws Exception {

        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Extract Technical Details",
                "Extract Technical Details Id", uuid);

        new ExtractTechnicalDetailsDAL().deleteExtractTechnicalDetails(uuid);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

}
