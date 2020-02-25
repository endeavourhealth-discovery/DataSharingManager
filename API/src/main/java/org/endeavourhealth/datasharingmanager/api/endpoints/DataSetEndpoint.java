package org.endeavourhealth.datasharingmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.MasterMappingDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonDataSet;
import org.endeavourhealth.core.database.dal.usermanager.caching.*;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.RdbmsCoreMasterMappingDal;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.*;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.datasharingmanager.api.DAL.DatasetDAL;
import org.endeavourhealth.datasharingmanager.api.Logic.RegionLogic;
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

@Path("/dataSet")
@Api(description = "API endpoint related to the DataSets")
public final class DataSetEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetEndpoint.class);
    private static final String DATASET_ID = "Data Set Id";

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);
    private static MasterMappingDalI masterMappingRepository = DalProvider.factoryDSMMasterMappingDal();


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSetEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Return either all data sets if no parameter is provided or search for " +
            "data sets using a UUID or a search term. Search matches on name of data sets. " +
            "Returns a JSON representation of the matching set of data sets")
    public Response getDataSet(@Context SecurityContext sc,
                              @ApiParam(value = "Optional uuid") @QueryParam("uuid") String uuid,
                              @ApiParam(value = "Optional search term") @QueryParam("searchData") String searchData
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Data Set(s)",
                "Data Set Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getDataSet - list");

            return getDataSetList();
        } else if (uuid != null){
            LOG.trace("getDataSet - single - " + uuid);
            return getSingleDataSet(uuid);
        } else {
            LOG.trace("Search Data Set - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSetEndpoint.Post")
    @Path("/")
    @ApiOperation(value = "Save a new data set or update an existing one.  Accepts a JSON representation " +
            "of a dataset.")
    @RequiresAdmin
    public Response postDataSet(@Context SecurityContext sc,
                                @HeaderParam("userProjectId") String userProjectId,
                               @ApiParam(value = "Json representation of data set to save or update") JsonDataSet dataSet
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Data set",
                "Data set", dataSet);

        if (dataSet.getUuid() != null) {
            new DatasetDAL().updateDataSet(dataSet, userProjectId, false);
        } else {
            dataSet.setUuid(UUID.randomUUID().toString());
            new DatasetDAL().saveDataSet(dataSet, userProjectId);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(dataSet.getUuid())
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSetEndpoint.Post")
    @Path("/updateMappings")
    @ApiOperation(value = "Updates the DPA mapping.  Accepts a JSON representation of a data set.")
    @RequiresAdmin
    public Response updateMappings(@Context SecurityContext sc,
                                     @HeaderParam("userProjectId") String userProjectId,
                                     @ApiParam(value = "Json representation of data set to save or update") JsonDataSet dataSet
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Data set",
                "Data set", dataSet);

        new DatasetDAL().updateDataSet(dataSet, userProjectId, true);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(dataSet.getUuid())
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSetEndpoint.Delete")
    @Path("/")
    @ApiOperation(value = "Delete a data set based on UUID that is passed to the API.  Warning! This is permanent.")
    @RequiresAdmin
    public Response deleteDataSet(@Context SecurityContext sc,
                                  @HeaderParam("userProjectId") String userProjectId,
                                  @ApiParam(value = "UUID of the data sets to be deleted") @QueryParam("uuids") List<String> uuids
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "data set",
                "data set Id", uuids);

        for (String uuid : uuids) {
            new DatasetDAL().deleteDataSet(uuid, userProjectId);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSetEndpoint.GetDataProcessingAgreements")
    @Path("/dpas")
    @ApiOperation(value = "Returns a list of Json representations of Data Processing Agreements that are linked " +
            "to the data set.  Accepts a UUID of a data set.")
    public Response getDpaForDataSet(@Context SecurityContext sc,
                                    @ApiParam(value = "UUID of dataset") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "data set Id", uuid);

        return getLinkedDpas(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSetEndpoint.GetDataSharingAgreements")
    @Path("/dsas")
    @ApiOperation(value = "Returns a list of Json representations of Data Sharing Agreements that are linked " +
            "to the data set.  Accepts a UUID of a data set.")
    public Response getDsaForDataSet(@Context SecurityContext sc,
                                    @ApiParam(value = "UUID of data set") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DSA(s)",
                DATASET_ID, uuid);

        return getLinkedDsas(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSetEndpoint.GetProjects")
    @Path("/projects")
    @ApiOperation(value = "Returns a list of Json representations of Data Sharing Agreements that are linked " +
            "to the data set.  Accepts a UUID of a data set.")
    public Response getProjectsForDataSet(@Context SecurityContext sc,
                                         @ApiParam(value = "UUID of data set") @QueryParam("uuid") String uuid
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Projects(s)",
                DATASET_ID, uuid);

        return getLinkedProjects(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.DataSetEndpoint.GetRegions")
    @Path("/regions")
    @ApiOperation(value = "Returns a list of Json representations of regions that are linked " +
            "to the data processing agreement.  Accepts a UUID of a data set.")
    public Response getLinkedRegionsForDataSet(@Context SecurityContext sc,
                                           @ApiParam(value = "UUID of data set") @QueryParam("uuid") String uuid,
                                           @ApiParam(value = "Optional user Id to restrict based on region") @QueryParam("userId") String userId
    ) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Region(s)",
                DATASET_ID, uuid);

        return getLinkedRegions(uuid, userId);
    }

    private Response getDataSetList() throws Exception {

        List<DataSetEntity> dataSets = new DatasetDAL().getAllDataSets();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataSets)
                .build();
    }

    private Response getSingleDataSet(String uuid) throws Exception {
        DataSetEntity dataSet = DataSetCache.getDataSetDetails(uuid);

        return Response
                .ok()
                .entity(dataSet)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataSetEntity> dataSets = new DatasetDAL().search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataSets)
                .build();
    }

    private Response getLinkedDpas(String datasetUuid) throws Exception {

        List<String> dpaUuids = masterMappingRepository.getParentMappings(datasetUuid, MapType.DATASET.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());

        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (!dpaUuids.isEmpty())
            ret = DataProcessingAgreementCache.getDPADetails(dpaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedDsas(String datasetUuid) throws Exception {

        List<String> dsaUuids = masterMappingRepository.getParentMappings(datasetUuid, MapType.DATASET.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());

        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUuids.isEmpty())
            ret = DataSharingAgreementCache.getDSADetails(dsaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedProjects(String datasetUuid) throws Exception {

        List<String> dsaUuids = masterMappingRepository.getParentMappings(datasetUuid, MapType.DATASET.getMapType(), MapType.PROJECT.getMapType());

        List<ProjectEntity> ret = new ArrayList<>();

        if (!dsaUuids.isEmpty())
            ret = ProjectCache.getProjectDetails(dsaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedRegions(String dsaUuid, String userId) throws Exception {

        List<String> regionUuids = masterMappingRepository.getParentMappings(dsaUuid, MapType.DATASET.getMapType(), MapType.REGION.getMapType());

        if (userId != null) {
            regionUuids = new RegionLogic().filterRegionsForUser(regionUuids, userId);
        }

        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = RegionCache.getRegionDetails(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}

