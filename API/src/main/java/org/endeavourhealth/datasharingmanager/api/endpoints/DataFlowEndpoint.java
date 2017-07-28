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
import org.endeavourhealth.datasharingmanager.api.database.MapType;
import org.endeavourhealth.datasharingmanager.api.database.models.DataFlowEntity;
import org.endeavourhealth.datasharingmanager.api.database.models.DataProcessingAgreementEntity;
import org.endeavourhealth.datasharingmanager.api.database.models.DataSharingAgreementEntity;
import org.endeavourhealth.datasharingmanager.api.database.models.MasterMappingEntity;
import org.endeavourhealth.datasharingmanager.api.json.JsonDataFlow;
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

@Path("/dataFlow")
@Metrics(registry = "EdsRegistry")
@Api(description = "API endpoint related to the data flow agreements")
public final class DataFlowEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DataFlowEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DataFlowEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return either all data flow agreements if no parameter is provided or search for " +
            "data flow agreements using a UUID or a search term. Search matches on name of data flow. " +
            "Returns a JSON representation of the matching set of Data Flows")
    public Response get(@Context SecurityContext sc,
                        @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                        @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Data Flow(s)",
                "Data Flow Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getDataFlow - list");

            return getDataFlowList();
        } else if (uuid != null){
            LOG.trace("getDataFlow - single - " + uuid);
            return getSingleDataFlow(uuid);
        } else {
            LOG.trace("Search Data Flow - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DataFlowEndpoint.Post")
    @Path("/")
    @ApiOperation(value = "Save a new data flow or update an existing one.  Accepts a JSON representation " +
            "of a data flow.")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc,
                         @ApiParam(value = "Json representation of data flow to save or update") JsonDataFlow dataFlow
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Data Flow",
                "Data Flow", dataFlow);

        if (dataFlow.getUuid() != null) {
            MasterMappingEntity.deleteAllMappings(dataFlow.getUuid());
            DataFlowEntity.updateDataFlow(dataFlow);
        } else {
            dataFlow.setUuid(UUID.randomUUID().toString());
            DataFlowEntity.saveDataFlow(dataFlow);
        }

        MasterMappingEntity.saveDataFlowMappings(dataFlow);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DataFlowEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete a data flow based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteOrganisation(@Context SecurityContext sc,
                                       @ApiParam(value = "UUID of the data flow to be deleted") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Data Flow",
                "Data Flow Id", uuid);

        DataFlowEntity.deleteDataFlow(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DataFlowEndpoint.GetDataProcessingAgreements")
    @Path("/dpas")
    @ApiOperation(value = "Returns a list of Json representations of Data Processing Agreements that are linked " +
            "to the data flow.  Accepts a UUID of a data flow.")
    public Response getLinkedDpas(@Context SecurityContext sc,
                                  @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "DPA Id", uuid);

        return getLinkedDpas(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DataFlowEndpoint.GetDataSharingAgreements")
    @Path("/dsas")
    @ApiOperation(value = "Returns a list of Json representations of Data Sharing Agreements that are linked " +
            "to the data flow.  Accepts a UUID of a data flow.")
    public Response getLinkedDsas(@Context SecurityContext sc,
                                  @ApiParam(value = "UUID of data flow") @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DSA(s)",
                "DSA Id", uuid);

        return getLinkedDsas(uuid);
    }

    private Response getDataFlowList() throws Exception {

        List<DataFlowEntity> dataFlows = DataFlowEntity.getAllDataFlows();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataFlows)
                .build();
    }

    private Response getSingleDataFlow(String uuid) throws Exception {
        DataFlowEntity dataFlow = DataFlowEntity.getDataFlow(uuid);

        return Response
                .ok()
                .entity(dataFlow)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataFlowEntity> dataflows = DataFlowEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataflows)
                .build();
    }

    private Response getLinkedDpas(String dataFlowUuid) throws Exception {

        List<String> dpaUuids = MasterMappingEntity.getChildMappings(dataFlowUuid, MapType.DATAFLOW.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (dpaUuids.size() > 0)
            ret = DataProcessingAgreementEntity.getDPAsFromList(dpaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedDsas(String dataFlowUuid) throws Exception {

        List<String> dsaUuids = MasterMappingEntity.getParentMappings(dataFlowUuid, MapType.DATAFLOW.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (dsaUuids.size() > 0)
            ret = DataSharingAgreementEntity.getDSAsFromList(dsaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
