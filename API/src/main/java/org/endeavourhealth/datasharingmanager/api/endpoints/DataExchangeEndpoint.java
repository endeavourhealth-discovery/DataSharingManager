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
import org.endeavourhealth.datasharingmanagermodel.models.database.*;
import org.endeavourhealth.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.datasharingmanagermodel.models.json.JsonDataExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/dataExchange")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to the data exchanges")
public class DataExchangeEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DataExchangeEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataExchangeEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return either all data exchanges if no parameter is provided or search for " +
            "data exchanges using a UUID or a search term. Search matches on name of data exchange. " +
            "Returns a JSON representation of the matching set of Data exchanges")
    public Response getDataExchange(@Context SecurityContext sc,
                                @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                                @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Data exchange(s)",
                "Data exchange Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getDataExchange - list");

            return getDataExchangeList();
        } else if (uuid != null){
            LOG.trace("getDataExchange - single - " + uuid);
            return getSingleDataExchange(uuid);
        } else {
            LOG.trace("Search Data Exchange - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataExchangeEndpoint.Post")
    @Path("/")
    @ApiOperation(value = "Save a new data exchange or update an existing one.  Accepts a JSON representation " +
            "of a data exchange.")
    @RequiresAdmin
    public Response postDataExchange(@Context SecurityContext sc,
                                 @ApiParam(value = "Json representation of data exchange to save or update") JsonDataExchange dataExchange
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Data exchange",
                "Data exchange", dataExchange);

        if (dataExchange.getUuid() != null) {
            MasterMappingEntity.deleteAllMappings(dataExchange.getUuid());
            DataExchangeEntity.updateDataExchange(dataExchange);
        } else {
            dataExchange.setUuid(UUID.randomUUID().toString());
            DataExchangeEntity.saveDataExchange(dataExchange);
        }
        
        MasterMappingEntity.saveDataExchangeMappings(dataExchange);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(dataExchange.getUuid())
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataExchangeEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete a data flow based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteDataExchange(@Context SecurityContext sc,
                                   @ApiParam(value = "UUID of the data exchange to be deleted") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Data exchange",
                "Data exchange Id", uuid);

        DataExchangeEntity.deleteDataExchange(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataExchangeEndpoint.GetLinkedDataFlow")
    @Path("/dataFlows")
    @ApiOperation(value = "Returns a list of Json representations of Data Flow that are linked " +
            "to the data Exchange.  Accepts a UUID of a data exchange.")
    public Response getLinkedDataFlow(@Context SecurityContext sc,
                                             @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "DPA Id", uuid);

        return getLinkedDataFlows(uuid);
    }

    private Response getDataExchangeList() throws Exception {

        List<DataExchangeEntity> dataExchanges = DataExchangeEntity.getAllDataExchanges();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataExchanges)
                .build();
    }

    private Response getSingleDataExchange(String uuid) throws Exception {
        DataExchangeEntity dataExchange = DataExchangeEntity.getDataExchange(uuid);

        return Response
                .ok()
                .entity(dataExchange)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataExchangeEntity> dataExchanges = DataExchangeEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataExchanges)
                .build();
    }

    private Response getLinkedDataFlows(String dateExchangeId) throws Exception {

        List<String> dataFlowUuids = MasterMappingEntity.getParentMappings(dateExchangeId, MapType.DATAEXCHANGE.getMapType(), MapType.DATAFLOW.getMapType());
        List<DataFlowEntity> ret = new ArrayList<>();

        if (!dataFlowUuids.isEmpty())
            ret = DataFlowEntity.getDataFlowsFromList(dataFlowUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }
}
