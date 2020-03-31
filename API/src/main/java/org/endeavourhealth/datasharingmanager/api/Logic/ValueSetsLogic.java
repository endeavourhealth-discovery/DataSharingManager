package org.endeavourhealth.datasharingmanager.api.Logic;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datasharingmanager.ValueSetsDalI;
import org.endeavourhealth.core.database.dal.datasharingmanager.models.JsonValueSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.List;

public class ValueSetsLogic {

    private Integer defaultPageNumber = 1;
    private Integer defaultPageSize = 20;
    private String defaultOrderColumn = "name";
    private String defaultSearchData = "";

    private static ValueSetsDalI valueSetsRepository = DalProvider.factoryDSMValueSetsDal();

    private static final Logger LOG = LoggerFactory.getLogger(ValueSetsLogic.class);

    public ValueSetsLogic() {
    }

    public List<JsonValueSets> getAllValueSets(String searchData, Integer pageNumber, Integer pageSize,
                                               String orderColumn, boolean descending) throws Exception {

        if (pageNumber == null)
            pageNumber = defaultPageNumber;
        if (pageSize == null)
            pageSize = defaultPageSize;
        if (orderColumn == null)
            orderColumn = defaultOrderColumn;
        if (searchData == null)
            searchData = defaultSearchData;

        return valueSetsRepository.getAllValueSets(searchData, pageNumber, pageSize, orderColumn, descending);
    }

    public Response getTotalNumber(String expression) throws Exception {
        Long count = valueSetsRepository.getTotalNumber(expression);
        return Response
                .ok()
                .entity(count)
                .build();
    }

    /*
    public JsonValueSets saveCodeSet(JsonValueSets jsonValueSets, boolean isEdit) throws Exception {

        SecurityValueSetsDAL dal = new SecurityValueSetsDAL();
        if (isEdit) {

            ValueSetsEntity entity = new ValueSetsEntity();
            entity.setUuid(jsonValueSets.getUuid());
            entity.setName(jsonValueSets.getName());

            dal.updateValuesSets(entity);
            dal.deleteValueSetsCodes(jsonValueSets.getUuid());
            dal.createValueSetsCodes(parseCodeSetCodes(jsonValueSets.getValuesSetCodes()));

            return dal.parseEntityToJson(entity);

        } else {

            ValueSetsEntity entity = new ValueSetsEntity();
            entity.setUuid(UUID.randomUUID().toString());
            entity.setName(jsonValueSets.getName());
            entity = dal.createValuesSets(entity);

            dal.createValueSetsCodes(parseCodeSetCodes(jsonValueSets.getValuesSetCodes()));

            return dal.parseEntityToJson(entity);
        }
    }

    public void deleteCodeSet(List<String> ids) throws Exception {
        SecurityValueSetsDAL dal = new SecurityValueSetsDAL();
        for (String id : ids) {
            dal.deleteValueSetsCodes(id);
            dal.deleteValuesSets(id);
        }
    }

    private ArrayList<ValueSetsCodesEntity> parseCodeSetCodes(JsonValueSetCodes[] codes) {

        ArrayList<ValueSetsCodesEntity> codeEntities = new ArrayList();
        for (JsonValueSetCodes code : codes) {
            ValueSetsCodesEntity codeEntity = new ValueSetsCodesEntity();
            // codeEntity.setUid(UUID.randomUUID().toString());
            // codeEntity.setValueSetsId(code.getValueSetId());
            codeEntity.setRead2ConceptId(code.getRead2ConceptId());
            codeEntity.setCtv3ConceptId(code.getCtv3ConceptId());
            codeEntity.setSctConceptId(code.getSctConceptId());
            codeEntities.add(codeEntity);
        }
        return codeEntities;
    }
    */
}
