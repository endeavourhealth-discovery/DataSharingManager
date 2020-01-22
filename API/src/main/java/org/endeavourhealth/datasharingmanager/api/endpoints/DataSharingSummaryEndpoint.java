package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.DataSharingSummaryEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonDataSharingSummary;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.DAL.DataSharingSummaryDAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.UUID;

@Path("/dataSharingSummary")
@Api(description = "API endpoint related to the data sharing summaries")
public final class DataSharingSummaryEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DataSharingSummaryEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSharingSummaryEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return either all data sharing summaries if no parameter is provided or search for " +
            "data sharing summaries using a UUID or a search term. Search matches on name or description of data sharing summary. " +
            "Returns a JSON representation of the matching set of data sharing summaries")
    public Response getDataSharingSummary(@Context SecurityContext sc,
                        @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                        @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Data Sharing Summary(s)",
                "Data Sharing Summary Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getData Sharing Summary - list");
            return getDataSharingSummaryList();
        } else if (uuid != null){
            LOG.trace("getData Sharing Summary - single - " + uuid);
            Response response = getSingleDataSharingSummary(uuid);
            return response;
            //return getSingleDataSharingSummary(uuid);
        } else {
            LOG.trace("Search DPA - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSharingSummaryEndpoint.Post")
    @Path("/")
    @ApiOperation(value = "Save a new data sharing summary or update an existing one.  Accepts a JSON representation " +
            "of a data sharing summary.")
    @RequiresAdmin
    public Response postDataSharingSummary(@Context SecurityContext sc,
                         @ApiParam(value = "Json representation of data sharing summary to save or update") JsonDataSharingSummary dataSharingSummary
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Data Sharing Summary",
                "Data Sharing Summary", dataSharingSummary);

        if (dataSharingSummary.getUuid() != null) {
            new DataSharingSummaryDAL().updateDataSharingSummary(dataSharingSummary);
        } else {
            dataSharingSummary.setUuid(UUID.randomUUID().toString());
            new DataSharingSummaryDAL().saveDataSharingSummary(dataSharingSummary);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSharingSummaryEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete a data flow based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteDataSharingSummary(@Context SecurityContext sc,
                                             @ApiParam(value = "UUID of the data sharing summaries to be deleted") @QueryParam("uuids") List<String> uuids

    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Data Sharing Summary",
                "Data Sharing Summary Id", uuids);

        for (String uuid : uuids) {
            new DataSharingSummaryDAL().deleteDataSharingSummary(uuid);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    private Response getDataSharingSummaryList() throws Exception {

        List<DataSharingSummaryEntity> dataSharingSummaries = new DataSharingSummaryDAL().getAllDataSharingSummaries();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataSharingSummaries)
                .build();
    }

    private Response getSingleDataSharingSummary(String uuid) throws Exception {
        DataSharingSummaryEntity dataSharingSummary = new DataSharingSummaryDAL().getDataSharingSummary(uuid);

        return Response
                .ok()
                .entity(dataSharingSummary)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataSharingSummaryEntity> datasharingsummaryEntities = new DataSharingSummaryDAL().search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(datasharingsummaryEntities)
                .build();
    }

}
