package com.dair.cais.type;

import com.dair.exception.CaisBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlertTypeServiceExtended {

    @Autowired
    private AlertTypeMapperExtended typeMapper;

    @Autowired
    private AlertTypeMapperExtended typeMapperExtended;

    @Autowired
    private AlertTypeRepositoryExtended typeRepository;

    public AlertTypeExtended getAlertTypeFields(final String alertTypeId) {
        AlertTypeEntityExtended typeById = typeRepository.getAlertTypeFields(alertTypeId);
        return typeMapper.toModel(typeById);
    }

    public Map<String, Object> fetchalertTypesAll() {

        try {
            List<AlertTypeEntityExtended> allAlertTypeEntities = typeRepository.fetchAllAlertTypes();

            List<AlertTypeExtended> allAlertTypes = allAlertTypeEntities.stream().map(a -> typeMapperExtended.toModel(a))
                    .collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("types", allAlertTypes);
            response.put("count", allAlertTypes.size());
            return response;
        } catch (Exception e) {
            throw new CaisBaseException("Error retrieving types");
        }
    }
}
