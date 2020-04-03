package org.endeavourhealth.datasharingmanager.api.Logic;

import com.google.common.base.Strings;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.MasterMappingDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.OrganisationDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.MapType;
import org.endeavourhealth.core.database.dal.datasharingmanager.enums.OrganisationType;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonFileUpload;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonOrganisation;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonStatistics;
import org.endeavourhealth.core.database.dal.usermanager.caching.*;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.*;
import org.endeavourhealth.datasharingmanager.api.DAL.*;
import org.endeavourhealth.datasharingmanager.api.utility.CsvHelper;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class OrganisationLogic {
    private static List<MasterMappingEntity> bulkUploadMappings = new ArrayList<>();
    private static HashMap<String, String> bulkOrgMap = new HashMap<>();
    private static HashMap<String, List<String>> childParentMap = new HashMap<>();
    private static boolean uploadInProgress = false;

    private Integer defaultPageNumber = 1;
    private Integer defaultPageSize = 20;
    private String defaultOrderColumn = "name";
    private String defaultSearchData = "";

    private EntityManager _entityManager;
    private MasterMappingDAL _masterMappingDAL;

    private static MasterMappingDalI masterMappingRepository = DalProvider.factoryDSMMasterMappingDal();
    private static OrganisationDalI organisationRepository = DalProvider.factoryDSMOrganisationDal();

    public OrganisationLogic() throws Exception {
        _entityManager = ConnectionManager.getDsmEntityManager();
        _masterMappingDAL = new MasterMappingDAL(_entityManager);
    }

    public Response getOrganisation(String uuid, String searchData, String searchType, byte organisationType,
                Integer pageNumber, Integer pageSize, String orderColumn, boolean descending, UUID userId ) throws Exception {


        boolean searchServices = false;
        byte orgType = -1;
        if (searchType != null && searchType.equals("services"))
            searchServices = true;

        if (pageNumber == null)
            pageNumber = defaultPageNumber;
        if (pageSize == null)
            pageSize = defaultPageSize;
        if (orderColumn == null)
            orderColumn = defaultOrderColumn;
        if (searchData == null)
            searchData = defaultSearchData;

        if (uuid != null) {
            return getSingleOrganisation(uuid);
        } else if (!Strings.isNullOrEmpty(searchData)) {
            return searchOrganisations(searchData, searchServices, organisationType, pageNumber, pageSize, orderColumn, descending, userId);
        } else {
            return getOrganisations(searchData, searchServices, organisationType, pageNumber, pageSize, orderColumn, descending);
        }
    }

    public Response postOrganisation(JsonOrganisation organisation, String userProjectId) throws Exception {


        if (organisation.getUuid() != null) {
            new OrganisationDAL().updateOrganisation(organisation, userProjectId, false);
        } else {
            String hashString = organisation.getName() + organisation.getOdsCode();
            if (organisation.getIsService().equals("1")) {
                hashString += "service";
            }
            organisation.setUuid(UUID.nameUUIDFromBytes(hashString.getBytes()).toString());
            new OrganisationDAL().saveOrganisation(organisation, userProjectId);
        }

        return Response
                .ok()
                .entity(organisation.getUuid())
                .build();

    }

    public Response updateMappings(JsonOrganisation organisation, String userProjectId) throws Exception {
        new OrganisationDAL().updateOrganisation(organisation, userProjectId, true);
        return Response
                .ok()
                .entity(organisation.getUuid())
                .build();
    }

    public Response getOrganisationsByType(byte type) throws Exception {
        List<OrganisationEntity> organisations = new OrganisationDAL().getOrganisationByType(type);

        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    public Response getOrganisationTypes() throws Exception {
        List<OrganisationTypeEntity> organisationTypes = new OrganisationTypeDAL().getAllOrganisationTypes();

        return Response
                .ok()
                .entity(organisationTypes)
                .build();
    }

    public Response getTotalNumberOfOrganisations(String expression, boolean searchServices) throws Exception {
        Long count = new OrganisationDAL().getTotalNumberOfOrganisations(expression, searchServices);

        return Response
                .ok()
                .entity(count)
                .build();
    }

    private Response getSingleOrganisation(String uuid) throws Exception {
        OrganisationEntity organisationEntity = OrganisationCache.getOrganisationDetails(uuid);

        return Response
                .ok()
                .entity(organisationEntity)
                .build();

    }

    private Response getOrganisations(String searchData, boolean searchServices, byte organisationType,
                                      Integer pageNumber, Integer pageSize,
                                      String orderColumn, boolean descending) throws Exception {

        List<OrganisationEntity> organisations = new OrganisationDAL().getOrganisations(searchData, searchServices,
                organisationType, pageNumber, pageSize, orderColumn, descending);

        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    private Response searchOrganisations(String searchData, boolean searchServices, byte organisationType,
                                         Integer pageNumber, Integer pageSize,
                                         String orderColumn, boolean descending, UUID userId) throws Exception {

        List<OrganisationEntity> organisations = organisationRepository.searchOrganisations(searchData, searchServices,
                organisationType, pageNumber, pageSize, orderColumn, descending, userId);

        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    public Response getRegionsForOrganisation(String organisationUuid, String userId) throws Exception {

        List<String> regionUuids = masterMappingRepository.getParentMappings(organisationUuid, MapType.ORGANISATION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (userId != null) {
            regionUuids = new RegionLogic().filterRegionsForUser(regionUuids, userId);
        }

        if (!regionUuids.isEmpty())
            ret = RegionCache.getRegionDetails(regionUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getDPAsOrganisationPublishingTo(String organisationUuid) throws Exception {

        List<String> dpaUUIDs = masterMappingRepository.getParentMappings(organisationUuid, MapType.PUBLISHER.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (!dpaUUIDs.isEmpty())
            ret = DataProcessingAgreementCache.getDPADetails(dpaUUIDs);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getDPAsOrganisationPublishingToFromList(List<String> organisationUuids) throws Exception {

        List<String> dpaUUIDs = masterMappingRepository.getParentMappings(organisationUuids, MapType.PUBLISHER.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());
        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (!dpaUUIDs.isEmpty())
            ret = DataProcessingAgreementCache.getDPADetails(dpaUUIDs);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getProjectsOrganisationPublishingToFromList(List<String> organisationUuids) throws Exception {

        List<String> projectUUIDs = masterMappingRepository.getParentMappings(organisationUuids, MapType.PUBLISHER.getMapType(), MapType.PROJECT.getMapType());
        List<ProjectEntity> ret = new ArrayList<>();

        if (!projectUUIDs.isEmpty())
            ret = ProjectCache.getProjectDetails(projectUUIDs);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getDSAsOrganisationSubscribingTo(String organisationUuid) throws Exception {

        List<String> dsaUuids = masterMappingRepository.getParentMappings(organisationUuid, MapType.SUBSCRIBER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUuids.isEmpty())
            ret = DataSharingAgreementCache.getDSADetails(dsaUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getDSAsOrganisationSubscribingToFromList(List<String> organisationUuids) throws Exception {

        List<String> dsaUuids = masterMappingRepository.getParentMappings(organisationUuids, MapType.SUBSCRIBER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUuids.isEmpty())
            ret = DataSharingAgreementCache.getDSADetails(dsaUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getDSAsOrganisationPublishingTo(String organisationUuid) throws Exception {

        List<String> dsaUUIds = masterMappingRepository.getParentMappings(organisationUuid, MapType.PUBLISHER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUUIds.isEmpty())
            ret = DataSharingAgreementCache.getDSADetails(dsaUUIds);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getDSAsOrganisationPublishingToFromList(List<String> organisationUuids) throws Exception {

        List<String> dsaUUIds = masterMappingRepository.getParentMappings(organisationUuids, MapType.PUBLISHER.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (!dsaUUIds.isEmpty())
            ret = DataSharingAgreementCache.getDSADetails(dsaUUIds);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response generateStatistics(String type) throws Exception {
        List<JsonStatistics> stats = new OrganisationDAL().getStatisticsForType(type);

        return Response
                .ok()
                .entity(stats)
                .build();
    }

    public Response getOrganisationAddressList(String uuid) throws Exception {

        List<AddressEntity> addresses = new AddressDAL().getAddressesForOrganisation(uuid);

        return Response
                .ok()
                .entity(addresses)
                .build();
    }

    public Response getChildOrganisations(String organisationUuid, Short organisationType) throws Exception {

        List<String> organisationUuids = masterMappingRepository.getChildMappings(organisationUuid, MapType.ORGANISATION.getMapType(), organisationType);
        List<OrganisationEntity> ret = new ArrayList<>();

        if (!organisationUuids.isEmpty())
            ret = OrganisationCache.getOrganisationDetails(organisationUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getParentOrganisations(String organisationUuid, Short orgType) throws Exception {

        List<String> organisationUuids = masterMappingRepository.getParentMappings(organisationUuid, orgType, MapType.ORGANISATION.getMapType());
        List<OrganisationEntity> ret = new ArrayList<>();

        if (!organisationUuids.isEmpty())
            ret = OrganisationCache.getOrganisationDetails(organisationUuids);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public Response getMultipleOrganisationsFromODSList(List<String> odsCodes) throws Exception {

        List<OrganisationEntity> matchingOrganisations = OrganisationCache.getOrganisationDetailsFromOdsCodeList(odsCodes);

        return Response
                .ok()
                .entity(matchingOrganisations)
                .build();
    }

    private Response searchOrganisations(List<OrganisationEntity> baseOrganisationList, String searchTerm) throws Exception {

        List<OrganisationEntity> matchingOrgs = new ArrayList<>();

        if (!baseOrganisationList.isEmpty()) {

            matchingOrgs = baseOrganisationList.stream()
                    .filter(org -> org.getName().toLowerCase().contains(searchTerm.toLowerCase())
                            || org.getOdsCode().toLowerCase().contains(searchTerm.toLowerCase())).collect(Collectors.toList());
        }

        return Response
                .ok()
                .entity(matchingOrgs)
                .build();
    }

    public Response searchOrganisationsWithODSList(List<OrganisationEntity> baseOrganisationList, List<String> odsCodes) throws Exception {

        List<OrganisationEntity> matchingOrgs = new ArrayList<>();

        if (!baseOrganisationList.isEmpty()) {

            matchingOrgs = baseOrganisationList.stream()
                    .filter(org -> odsCodes.contains(org.getOdsCode()))
                    .collect(Collectors.toList());
        }

        return Response
                .ok()
                .entity(matchingOrgs)
                .build();
    }

    public Response searchOrganisationsInRegion(String regionUUID, String searchTerm, List<String> odsCodes) throws Exception {

        List<OrganisationEntity> orgsInAllChildRegions = RegionCache.getAllOrganisationsForAllChildRegions(regionUUID);

        if (searchTerm != null) {
            return searchOrganisations(orgsInAllChildRegions, searchTerm);
        } else {
            return searchOrganisationsWithODSList(orgsInAllChildRegions, odsCodes);
        }

    }

    public Response getAllOrganisationsInRegionAndChildRegions(String regionUUID) throws Exception {

        List<OrganisationEntity> orgsInAllChildRegions = RegionCache.getAllOrganisationsForAllChildRegions(regionUUID);

        return Response
                .ok()
                .entity(orgsInAllChildRegions)
                .build();

    }

    public Response searchPublishersInDSA(String dsaUUID, String searchTerm, List<String> odsCodes) throws Exception {

        List<String> organisationUuids =  masterMappingRepository.getChildMappings(dsaUUID, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.PUBLISHER.getMapType());

        List<OrganisationEntity> baseOrgs = new ArrayList<>();
        if (!organisationUuids.isEmpty())
            baseOrgs = OrganisationCache.getOrganisationDetails(organisationUuids);

        if (searchTerm != null) {
            return searchOrganisations(baseOrgs, searchTerm);
        } else {
            return searchOrganisationsWithODSList(baseOrgs, odsCodes);
        }
    }

    public Response searchSubscribersInDSA(String dsaUUID, String searchTerm, List<String> odsCodes) throws Exception {
        List<String> organisationUuids =  masterMappingRepository.getChildMappings(dsaUUID, MapType.DATASHARINGAGREEMENT.getMapType(), MapType.SUBSCRIBER.getMapType());

        List<OrganisationEntity> baseOrgs = new ArrayList<>();
        if (!organisationUuids.isEmpty())
            baseOrgs = OrganisationCache.getOrganisationDetails(organisationUuids);

        if (searchTerm != null) {
            return searchOrganisations(baseOrgs, searchTerm);
        } else {
            return searchOrganisationsWithODSList(baseOrgs, odsCodes);
        }
    }

    public Response startUpload() throws Exception {
        bulkUploadMappings.clear();
        bulkOrgMap.clear();
        childParentMap.clear();
        uploadInProgress = true;

        return Response
                .ok()
                .build();
    }

    public Response endUpload() throws Exception {

        bulkUploadMappings.clear();
        bulkOrgMap.clear();
        childParentMap.clear();
        uploadInProgress = false;

        return Response
                .ok()
                .build();
    }

    private boolean processFile(String filename) throws Exception {
        if (filename.contains("earchive")
                || filename.contains("ecarehomesucc")
                || filename.contains("econcur")
                || filename.contains("edconcur")
                || filename.contains("educationtype")
                || filename.contains("egdp")
                || filename.contains("egdppracmem")
                || filename.contains("egpcur")
                || filename.contains("egpmema")
                || filename.contains("enurse")
                || filename.contains("epcdp")
                || filename.contains("epcdpam")
                || filename.contains("epcdpaq")
                || filename.contains("epcmem")
                || filename.contains("epracmem")
                || filename.contains("ngpcur")
                || filename.contains("nlhscgpr")
                || filename.contains("scotgp")
                || filename.contains("scotmem")
                || filename.contains("succ")
                || filename.contains("wconcur")
                || filename.contains("egparc"))
            return false;


        return true;
    }

    public Response processCSVFile(JsonFileUpload file) throws Exception {
        boolean found = false;
        int orgsUploaded = 0;
        //OrganisationEntity.deleteUneditedBulkOrganisations();

        List<OrganisationEntity> updatedBulkOrganisations = new ArrayList<>(); // OrganisationEntity.getUpdatedBulkOrganisations();

        List<OrganisationEntity> organisationEntities = new ArrayList<>();
        List<AddressEntity> addressEntities = new ArrayList<>();


        System.out.println(file.getName());

        if (!processFile(file.getName())){
            return Response
                    .ok(organisationEntities.size())
                    .build();
        }

        if (file.getFileData() == null) {
            throw new Exception("No File Data transferred");
        }

        String csvData = file.getFileData();

        try (Scanner scanner = new Scanner(csvData)) {

            int i = 0;
            while (scanner.hasNext()) {
                i++;
                List<String> org = CsvHelper.parseLine(scanner.nextLine());

                OrganisationEntity importedOrg = createOrganisationEntity(org, file.getName());

                for (OrganisationEntity oe : updatedBulkOrganisations) {
                    if (oe.getUuid().equals(importedOrg.getUuid())) {
                        found = true;
                    }
                }

                if (found) {
                    //already have this org and it has been updated so set the conflicted UUID to the UUID of the original org and generate a new UUID
                    importedOrg.setBulkConflictedWith(importedOrg.getUuid());
                    importedOrg.setUuid(UUID.nameUUIDFromBytes((importedOrg.getName() + importedOrg.getOdsCode() + "conflict").getBytes()).toString());
                }

                if (bulkOrgMap.get(importedOrg.getOdsCode()) == null) {
                    organisationEntities.add(importedOrg);
                    addressEntities.add(createAddressEntity(org, importedOrg.getUuid()));
                    bulkOrgMap.put(importedOrg.getOdsCode(), importedOrg.getUuid());
                }

                List<String> parents;
                if (!org.get(14).equals("")) {
                    parents = childParentMap.getOrDefault(importedOrg.getOdsCode(), new ArrayList<>());
                    if (!parents.contains(org.get(14))) {
                        parents.add(org.get(14));
                        childParentMap.put(importedOrg.getOdsCode(), parents);
                    }
                }

                if (!org.get(2).equals("")) {
                    parents = childParentMap.getOrDefault(importedOrg.getOdsCode(), new ArrayList<>());
                    if (!parents.contains(org.get(2))) {
                        parents.add(org.get(2));
                        childParentMap.put(importedOrg.getOdsCode(), parents);
                    }
                }

                if (!org.get(3).equals("") ) {
                    parents = childParentMap.getOrDefault(importedOrg.getOdsCode(), new ArrayList<>());
                    if (!parents.contains(org.get(3))) {
                        parents.add(org.get(3));
                        childParentMap.put(importedOrg.getOdsCode(), parents);
                    }
                }

                if (i % 200 == 0 ) {
                    orgsUploaded += organisationEntities.size();
                    new OrganisationDAL().bulkSaveOrganisation(organisationEntities);
                    organisationEntities.clear();
                    new AddressDAL().bulkSaveAddresses(addressEntities);
                    addressEntities.clear();
                }


                found = false;
            }

            orgsUploaded += organisationEntities.size();
            new OrganisationDAL().bulkSaveOrganisation(organisationEntities);
            organisationEntities.clear();
            new AddressDAL().bulkSaveAddresses(addressEntities);
            addressEntities.clear();
        }

        return Response
                .ok(orgsUploaded)
                .build();
    }

    public Response saveBulkMappings(Integer limit) throws Exception {

        Integer i = 0;
        for (Iterator<Map.Entry<String, List<String>>> it = childParentMap.entrySet().iterator(); it.hasNext();) {
            i++;
            Map.Entry<String, List<String>> map = it.next();

            for (String parent : map.getValue()) {

                MasterMappingEntity mappingEntity = new MasterMappingEntity();
                mappingEntity.setChildUuid(bulkOrgMap.get(map.getKey()));
                mappingEntity.setChildMapTypeId(MapType.ORGANISATION.getMapType());
                mappingEntity.setParentUuid(bulkOrgMap.get(parent));
                mappingEntity.setParentMapTypeId(MapType.ORGANISATION.getMapType());

                if (mappingEntity.getParentUuid() != null && mappingEntity.getChildUuid() != null)
                    bulkUploadMappings.add(mappingEntity);
            }

            if (i % 200 == 0 ) {
                _masterMappingDAL.bulkSaveMappings(bulkUploadMappings);
                bulkUploadMappings.clear();
            }

            it.remove();
            if (i > limit)
                break;
        }

        /*childParentMap.forEach((k, v) -> {
            MasterMappingEntity map = new MasterMappingEntity();
            map.setChildUuid(bulkOrgMap.get(k));
            map.setChildMapTypeId(MapType.ORGANISATION.getMapType());
            map.setParentUuid(bulkOrgMap.get(v));
            map.setParentMapTypeId(MapType.ORGANISATION.getMapType());

            if (map.getParentUuid() != null && map.getChildUuid() != null)
                bulkUploadMappings.add(map);
        });*/

        if (!bulkUploadMappings.isEmpty()) {
            _masterMappingDAL.bulkSaveMappings(bulkUploadMappings);
            bulkUploadMappings.clear();
        }

        return Response
                .ok(childParentMap.size())
                .build();
    }

    private byte getOrgTypeFromFilename(String filename, String odsCode) throws Exception {

        String file = filename.toLowerCase();

        if (file.contains("epraccur"))
            return OrganisationType.GPPRACTICE.getOrganisationType(); // "GP Practice";

        if (file.contains("etrust")) {
            if (odsCode.length() <= 3)
                return OrganisationType.NHSTRUST.getOrganisationType(); // "NHS Trust";
            else
                return OrganisationType.NHSTRUSTSITE.getOrganisationType(); // "NHS Trust Site";
        }

        if (file.contains("etr"))
            return OrganisationType.NHSTRUST.getOrganisationType(); // "NHS Trust";

        if (file.contains("plab"))
            return OrganisationType.PATHLAB.getOrganisationType(); // "Pathology Laboratories";

        if (file.contains("epracarc"))
            return OrganisationType.GPPRACTICE.getOrganisationType(); // "Archived GP Practice";

        if (file.contains("branch"))
            return OrganisationType.BRANCH.getOrganisationType(); // "Branch";

        if (file.contains("auth"))
            return OrganisationType.COMMISSIONINGREGION.getOrganisationType(); // "Commissioning Region";

        if (file.contains("ecare") && odsCode.length() > 3)
            return OrganisationType.CARETRUSTSITE.getOrganisationType(); // "Care Trust Site";

        if (file.contains("ecare") && odsCode.length() <= 3)
            return OrganisationType.CARETRUST.getOrganisationType(); // "Care Trust";

        if (file.contains("ccgsite"))
            return OrganisationType.CCGSITE.getOrganisationType(); // "CCG Site";

        if (file.contains("ccg"))
            return OrganisationType.CCG.getOrganisationType(); // "CCG";

        if (file.contains("csuaq"))
            return OrganisationType.CSU.getOrganisationType(); // "CSU";

        if (file.contains("csusite"))
            return OrganisationType.CSUSITE.getOrganisationType(); // "CSU Site";

        if (file.equals("ect.csv"))
            return OrganisationType.CARETRUST.getOrganisationType(); // "Care Trust";

        if (file.contains("ectsite"))
            return OrganisationType.CARETRUSTSITE.getOrganisationType(); // "Care Trust Site";

        if (file.contains("dispensary"))
            return OrganisationType.DISPENSARY.getOrganisationType(); // "Dispensary";

        if (file.contains("educate"))
            return OrganisationType.EDUCATION.getOrganisationType(); // "Education Establishment";

        if (file.contains("egp"))
            return OrganisationType.GPPRACTICE.getOrganisationType(); // "GP Practice";

        if (file.contains("hospice")) {
            if (odsCode.length() > 3)
                return OrganisationType.NONNHSHOSPICE.getOrganisationType(); // "Non NHS Hospice";
            if (odsCode.length() <= 3)
                return OrganisationType.NHSHOSPICE.getOrganisationType(); // "NHS Hospice";
        }

        if (file.contains("iom")) {
            if (odsCode.length() <= 3) {
                if (odsCode.substring(1, 1).equals("K"))
                    return OrganisationType.IOMGOVDIRECTORATE.getOrganisationType(); // "IoM Government Directorate";
                else
                    return OrganisationType.IOMGOVDEPT.getOrganisationType(); // "IoM Government Department";
            } else
                return OrganisationType.IOMGOVDIRECTORATESITE.getOrganisationType(); // "IoM Government Directorate Site";

        }

        if (file.contains("justice"))
            return OrganisationType.JUSTICEENTITY.getOrganisationType(); // "Justice Entity";

        if (file.contains("nonnhs"))
            return OrganisationType.NONNHSORGANISATION.getOrganisationType(); // "Non NHS Organisation";

        if (file.equals("ensa.csv"))
            return OrganisationType.NHSSUPPORTAGENGY.getOrganisationType(); // "NHS Support Agency and Shared Service";

        if (file.contains("eopthq"))
            return OrganisationType.OPTICALHQ.getOrganisationType(); // "Optical Headquarters";

        if (file.contains("eoptsite"))
            return OrganisationType.OPTICALSITE.getOrganisationType(); // "Optical Site";

        if (file.contains("other"))
            return OrganisationType.OTHER.getOrganisationType(); // "Other";

        if (file.contains("pharmacyhq"))
            return OrganisationType.PHARMACYHQ.getOrganisationType(); // "Pharmacy Headquarters";

        if (file.contains("ephpsite"))
            return OrganisationType.ISHPSITE.getOrganisationType(); // "ISHP Site";

        if (file.contains("ephp"))
            return OrganisationType.ISHP.getOrganisationType(); // "ISHP";

        if (file.contains("prison"))
            return OrganisationType.PRISON.getOrganisationType(); // "Prison";

        if (file.contains("school"))
            return OrganisationType.SCHOOL.getOrganisationType(); // "School";

        if (file.contains("spha"))
            return OrganisationType.SPECIALHEALTH.getOrganisationType(); // "Special Health Authority";

        if (file.contains("lauthsite"))
            return OrganisationType.LOCALAUTHORITYSITE.getOrganisationType(); // "Local Authority Site";

        if (file.contains("lauth"))
            return OrganisationType.LOCALAUTHORITY.getOrganisationType(); // "Local Authority";

        if (file.contains("niarchive") || file.contains("niorg"))
            return OrganisationType.NIORG.getOrganisationType(); // "NI Organisation";

        if (file.contains("scotgp"))
            return OrganisationType.SCOTTISHGP.getOrganisationType(); // "Scottish GP Practice";

        if (file.contains("scotorg"))
            return OrganisationType.SCOTTISHPROVIDER.getOrganisationType(); // "Scottish Provider Organisation";

        if (file.contains("whbs")) {
            if (odsCode.length() > 3)
                return OrganisationType.WALESHBSITE.getOrganisationType(); // "Wales Health Board Site";
            else
                return OrganisationType.WALESHB.getOrganisationType(); // "Wales Health Board";
        }

        return OrganisationType.OTHER.getOrganisationType(); // "Unknown";
    }

    private OrganisationEntity createOrganisationEntity(List<String> org, String filename) throws Exception {

        OrganisationEntity organisationEntity = new OrganisationEntity();
        organisationEntity.setName(org.get(1));
        organisationEntity.setOdsCode(org.get(0));
        organisationEntity.setUuid(UUID.nameUUIDFromBytes((organisationEntity.getName() + organisationEntity.getOdsCode()).getBytes()).toString());

        organisationEntity.setIsService((byte)0);
        organisationEntity.setBulkImported((byte)1);
        organisationEntity.setBulkItemUpdated((byte)0);
        organisationEntity.setType(getOrgTypeFromFilename(filename, org.get(0)));
        if (filename.equals("epracarc"))
            organisationEntity.setActive((byte)0);
        else
            organisationEntity.setActive((byte)1);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date = format.parse(org.get(10));

        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        organisationEntity.setDateOfRegistration(java.sql.Date.valueOf(localDate));

        return organisationEntity;
    }

    private AddressEntity createAddressEntity(List<String> org, String organisationUuid) throws Exception {

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setOrganisationUuid(organisationUuid);

        addressEntity.setBuildingName(org.get(4));
        addressEntity.setNumberAndStreet(org.get(5));
        addressEntity.setLocality(org.get(6));
        addressEntity.setCity(org.get(7));
        addressEntity.setCounty(org.get(8));
        addressEntity.setPostcode(org.get(9));

        return addressEntity;
    }

    public void deleteOrganisation(List<String> uuids, String userProjectId, UUID userId) throws Exception {
        for (String uuid : uuids) {
            new OrganisationDAL().deleteOrganisation(uuid, userProjectId);
        }
        Map<UUID, String> cache = organisationRepository.getCachedSearchTerm();
        if (cache != null && cache.containsKey(userId)) {
            cache.remove(userId);
        }
    }
}
