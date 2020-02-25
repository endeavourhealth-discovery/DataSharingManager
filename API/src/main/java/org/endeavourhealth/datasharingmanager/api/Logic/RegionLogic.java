package org.endeavourhealth.datasharingmanager.api.Logic;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.MasterMappingDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonRegion;
import org.endeavourhealth.core.database.dal.usermanager.caching.*;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DataProcessingAgreementEntity;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DataSharingAgreementEntity;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.OrganisationEntity;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.RegionEntity;
import org.endeavourhealth.core.database.rdbms.usermanager.models.UserRegionEntity;
import org.endeavourhealth.datasharingmanager.api.DAL.*;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RegionLogic {
    private static MasterMappingDalI masterMappingRepository = DalProvider.factoryDSMMasterMappingDal();

    public Response getRegion(String uuid, String searchData, String userId) throws Exception {

        if (uuid == null && searchData == null && userId == null) {
            return getRegionList();
        } else if (userId != null) {
            return getRegionListFilterOnRegion(userId);
        }  else if (uuid != null){
            return getSingleRegion(uuid);
        } else {
            return search(searchData);
        }
    }

    private Response getRegionListFilterOnRegion(String userId) throws Exception {

        UserRegionEntity userRegion = UserCache.getUserRegion(userId);
        System.out.println("region:"+userRegion.getRegionId());
        List<RegionEntity> dpas = RegionCache.getAllChildRegionsForRegion(userRegion.getRegionId());

        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    public Response postRegion(JsonRegion region, String userProjectID) throws Exception {

        if (region.getUuid() != null) {
            new RegionDAL().updateRegion(region, userProjectID, false);
        } else {
            region.setUuid(UUID.randomUUID().toString());
            new RegionDAL().saveRegion(region, userProjectID);
        }

        return Response
                .ok()
                .entity(region.getUuid())
                .build();
    }

    public Response updateMappings(JsonRegion region, String userProjectID) throws Exception {

        new RegionDAL().updateRegion(region, userProjectID, true);

        return Response
                .ok()
                .entity(region.getUuid())
                .build();
    }

    private Response getRegionList() throws Exception {

        List<RegionEntity> regions = RegionCache.getAllRegions();

        return Response
                .ok()
                .entity(regions)
                .build();
    }

    private Response getSingleRegion(String uuid) throws Exception {
        RegionEntity regionEntity = RegionCache.getRegionDetails(uuid);

        return Response
                .ok()
                .entity(regionEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<RegionEntity> regions = new RegionDAL().search(searchData);

        return Response
                .ok()
                .entity(regions)
                .build();
    }

    public Response getRegionOrganisations(String regionUUID) throws Exception {

        List<String> organisationUuids = masterMappingRepository.getChildMappings(regionUUID, MapType.REGION.getMapType(), MapType.ORGANISATION.getMapType());
        List<OrganisationEntity> ret = new ArrayList<>();

        if (!organisationUuids.isEmpty())
            ret = OrganisationCache.getOrganisationDetails(organisationUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public List<String> filterRegionsForUser(List<String> regions, String userId) throws Exception {

        UserRegionEntity userRegion = UserCache.getUserRegion(userId);

        List<RegionEntity> regionsForUsers = RegionCache.getAllChildRegionsForRegion(userRegion.getRegionId());

        Set<String> regionForUserSet =
                regionsForUsers.stream()
                        .map(RegionEntity::getUuid)
                        .collect(Collectors.toSet());

        regions = regions.stream().filter(reg -> regionForUserSet.contains(reg)).collect(Collectors.toList());

        return regions;

    }

    public Response getParentRegions(String regionUuid, String userId) throws Exception {

        List<String> regionUuids = masterMappingRepository.getParentMappings(regionUuid, MapType.REGION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (userId != null) {
            regionUuids = filterRegionsForUser(regionUuids, userId);
        }

        if (!regionUuids.isEmpty())
            ret = RegionCache.getRegionDetails(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getChildRegions(String regionUuid) throws Exception {

        List<String> regionUuids = masterMappingRepository.getChildMappings(regionUuid, MapType.REGION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (!regionUuids.isEmpty())
            ret = RegionCache.getRegionDetails(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getSharingAgreements(String regionUuid) throws Exception {

        List<String> sharingAgreementUuids = masterMappingRepository.getChildMappings(regionUuid, MapType.REGION.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!sharingAgreementUuids.isEmpty())
            ret = DataSharingAgreementCache.getDSADetails(sharingAgreementUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getProcessingAgreements(String regionUuid) throws Exception {

        List<String> processingAgreementUuids = masterMappingRepository.getChildMappings(regionUuid, MapType.REGION.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (!processingAgreementUuids.isEmpty())
            ret = new DataProcessingAgreementCache().getDPADetails(processingAgreementUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}
