package org.endeavourhealth.datasharingmanager.api.endpoints;


import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.Logic.ReportLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/report")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to the regions")
public class ReportEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ReportEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ReportEndpoint.getPublisherReport")
    @Path("/getPublisherReport")
    @ApiOperation(value = "Returns the data for the publisher report")
    public Response getPublisherReport(@Context SecurityContext sc,
                                       @ApiParam(value = "ODS Codes") @QueryParam("odsCodes") List<String> odsCodes,
                                       @ApiParam(value = "AgreementName") @QueryParam("agreementName") String agreementName
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Organisation(s)",
                "odsCodes", odsCodes,
                "agreementName", agreementName);

        clearLogbackMarkers();
        return new ReportLogic().getPublisherReport(odsCodes, agreementName);

    }

}
