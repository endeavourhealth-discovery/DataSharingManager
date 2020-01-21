package org.endeavourhealth.datasharingmanager.api.endpoints;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonValueSets;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.Logic.ValueSetsLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/value_sets")
@Api(description = "Api for all calls relating to the Value Sets")
public class ValueSetsEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ValueSetsEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ValueSets.get")
    @Path("/")
    @ApiOperation(value = "Returns a list of all Value Sets") // operation description
    public Response get(@Context SecurityContext sc,
                        @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                        @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData,
                        @ApiParam(value = "Optional page number (defaults to 1 if not provided)") @QueryParam("pageNumber") Integer pageNumber,
                        @ApiParam(value = "Optional page size (defaults to 20 if not provided)")@QueryParam("pageSize") Integer pageSize,
                        @ApiParam(value = "Optional order column (defaults to name if not provided)")@QueryParam("orderColumn") String orderColumn,
                        @ApiParam(value = "Optional ordering direction (defaults to ascending if not provided)") @QueryParam("descending") boolean descending) throws Exception {

        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "ValueSet(s)",
                "ValueSet Id", uuid,
                "SearchData", searchData);

        clearLogbackMarkers();

        List<JsonValueSets> result = new ValueSetsLogic().getAllValueSets(searchData, pageNumber, pageSize,
                orderColumn, descending);

        return Response
                .ok(result)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.Organisation.searchCount")
    @Path("/searchCount")
    @ApiOperation(value = "When using server side pagination, this returns the total count of the results of the query")
    public Response getValueSetsSearchCount(@Context SecurityContext sc,
                                            @ApiParam(value = "expression to filter value sets by") @QueryParam("expression") String expression
    ) throws Exception {

        if (expression == null)
            expression = "";

        return new ValueSetsLogic().getTotalNumber(expression);
    }

    /*
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ValueSets.delete")
    @Path("/")
    @ApiOperation(value = "Delete a Code Set based on id that is passed to the API.  Warning! This is permanent.")
    public Response delete(@Context SecurityContext sc,
                                  @ApiParam(value = "IDs of the Value Sets to be deleted")
                                  @QueryParam("ids") List<String> ids) throws Exception {

        LOG.debug("Delete Code Set called");

        new ValueSetsLogic().deleteCodeSet(ids);

        return Response
                .ok()
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.ValueSets.save")
    @Path("/value_sets/save")
    @RequiresAdmin
    @ApiOperation(value = "Saves an extract or updates an existing extract")
    public Response save(@Context SecurityContext sc, JsonValueSets jsonValueSets,
                                @ApiParam(value = "edit mode") @QueryParam("editMode") String editMode) throws Exception {

        LOG.debug("Save CodeSet called");

        boolean isEdit = editMode.equals("1");
        //jsonValueSets = new ValueSetsLogic().saveCodeSet(jsonValueSets, isEdit);

        return Response
                .ok()
                .entity(jsonValueSets)
                .build();
    }
    */
}
