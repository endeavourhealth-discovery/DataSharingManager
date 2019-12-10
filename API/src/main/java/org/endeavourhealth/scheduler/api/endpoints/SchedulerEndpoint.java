package org.endeavourhealth.scheduler.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.scheduler.api.json.JsonSchedule;
import org.endeavourhealth.scheduler.api.logic.SchedulerLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/scheduler")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to the Scheduler")
public class SchedulerEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerEndpoint.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.SchedulerEndpoint.getDescription")
    @Path("/description")
    @ApiOperation(value = "Returns the descriptive Quartz Cron definition of the cron expression")
    public Response getDescription(@Context SecurityContext sc,
                                   @ApiParam(value = "Scheduler object") JsonSchedule schedule) throws Exception {

        super.setLogbackMarkers(sc);

        return new SchedulerLogic().getDescription(schedule);
    }

}
