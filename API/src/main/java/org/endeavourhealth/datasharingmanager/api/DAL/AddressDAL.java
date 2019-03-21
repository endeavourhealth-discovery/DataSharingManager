package org.endeavourhealth.datasharingmanager.api.DAL;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.AddressEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonAddress;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonMarker;
import org.endeavourhealth.common.security.usermanagermodel.models.ConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddressDAL {

    public List<AddressEntity> getAddressesForOrganisation(String uuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);
        Root<AddressEntity> rootEntry = cq.from(AddressEntity.class);

        Predicate predicate = cb.equal(rootEntry.get("organisationUuid"), uuid);
        cq.where(predicate);

        TypedQuery<AddressEntity> query = entityManager.createQuery(cq);
        List<AddressEntity> ret = query.getResultList();
        entityManager.close();
        return ret;
    }

    public void bulkSaveAddresses(List<AddressEntity> addressEntities) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        int batchSize = 50;

        entityManager.getTransaction().begin();

        for (int i = 0; i < addressEntities.size(); i++) {
            AddressEntity addressEntity = addressEntities.get(i);
            entityManager.merge(addressEntity);
            if (i % batchSize == 0){
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void saveAddress(JsonAddress address) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        AddressEntity addressEntity = new AddressEntity(address);
        addressEntity.setUuid(address.getUuid());
        entityManager.getTransaction().begin();
        entityManager.persist(addressEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void updateAddress(JsonAddress address) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        AddressEntity addressEntity = entityManager.find(AddressEntity.class, address.getUuid());
        entityManager.getTransaction().begin();
        addressEntity.setOrganisationUuid(address.getOrganisationUuid());
        addressEntity.setBuildingName(address.getBuildingName());
        addressEntity.setNumberAndStreet(address.getNumberAndStreet());
        addressEntity.setLocality(address.getLocality());
        addressEntity.setCity(address.getCity());
        addressEntity.setCounty(address.getCounty());
        addressEntity.setPostcode(address.getPostcode());
        addressEntity.setGeolocationReprocess((byte)0);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void updateGeolocation(JsonAddress address) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        AddressEntity addressEntity = entityManager.find(AddressEntity.class, address.getUuid());
        entityManager.getTransaction().begin();
        addressEntity.setLat(address.getLat());
        addressEntity.setLng(address.getLng());
        addressEntity.setGeolocationReprocess((byte)0);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    private List<Object[]> getAddressMarkers(String parentUUID, Short parentMapType, Short childMapType) throws Exception {

        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        Query query = entityManager.createQuery(
                "select o.name, a.lat, a.lng from OrganisationEntity o " +
                        "inner join AddressEntity a on a.organisationUuid = o.uuid " +
                        "inner join MasterMappingEntity mm on mm.childUuid = o.uuid and mm.childMapTypeId = :childMap " +
                        "where mm.parentUuid = :parentUuid " +
                        "and mm.parentMapTypeId = :parentMap");
        query.setParameter("parentUuid", parentUUID);
        query.setParameter("childMap", childMapType);
        query.setParameter("parentMap", parentMapType);

        List<Object[]> result = query.getResultList();

        entityManager.close();

        return result;
    }

    public Response getOrganisationMarkers(String regionUuid, Short parentMapType, Short childMapType) throws Exception {

        List<Object[]> markers = getAddressMarkers(regionUuid, parentMapType, childMapType);

        List<JsonMarker> ret = new ArrayList<>();

        for (Object[] marker : markers) {
            String name = marker[0].toString();
            Double lat = marker[1]==null?0.0:Double.parseDouble(marker[1].toString());
            Double lng = marker[2]==null?0.0:Double.parseDouble(marker[2].toString());

            JsonMarker jsonMarker = new JsonMarker();
            jsonMarker.setName(name);
            jsonMarker.setLat(lat);
            jsonMarker.setLng(lng);

            ret.add(jsonMarker);
        }

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public void getGeoLocationsForOrganisations(List<UUID> organisationUuids) throws Exception {

        for (UUID org : organisationUuids) {
            List<AddressEntity> addressEntities = new AddressDAL().getAddressesForOrganisation(org.toString());

            for (AddressEntity address : addressEntities) {
                if (address.getLat() != null && address.getLng() != null) {
                    continue;
                }
                JsonAddress jsonAddress = new JsonAddress(address);
                getGeolocation(jsonAddress);
            }
        }
    }

    public void deleteAddressForOrganisations(String organisationUuid) throws Exception {
        EntityManager entityManager = ConnectionManager.getDsmEntityManager();

        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery(
                "DELETE from AddressEntity a " +
                        "where a.organisationUuid = :orgUuid ");
        query.setParameter("orgUuid", organisationUuid);

        int deletedCount = query.executeUpdate();

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public void getGeolocation(JsonAddress address) throws Exception {
        Client client = ClientBuilder.newClient();

        JsonNode json = ConfigManager.getConfigurationAsJson("GoogleMapsAPI");
        String url = json.get("url").asText();
        String apiKey = json.get("apiKey").asText();

        WebTarget resource = client.target(url + address.getPostcode().replace(" ", "+") + "&key=" + apiKey);

        Invocation.Builder request = resource.request();
        request.accept(MediaType.APPLICATION_JSON_TYPE);

        Response response = request.get();

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            String s = response.readEntity(String.class);
            JsonParser parser = new JsonParser();
            JsonElement obj = parser.parse(s);
            JsonObject jo = obj.getAsJsonObject();
            if (jo.getAsJsonArray("results").size() > 0) {
                JsonElement results = jo.getAsJsonArray("results").get(0);
                JsonObject location = results.getAsJsonObject().getAsJsonObject("geometry").getAsJsonObject("location");

                address.setLat(Double.parseDouble(location.get("lat").toString()));
                address.setLng(Double.parseDouble(location.get("lng").toString()));

                new AddressDAL().updateGeolocation(address);
            }
        }
    }
}
