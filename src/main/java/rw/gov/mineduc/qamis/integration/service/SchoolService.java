package rw.gov.mineduc.qamis.integration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rw.gov.mineduc.qamis.integration.model.School;
import rw.gov.mineduc.qamis.integration.repository.SchoolRepository;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SchoolService {

    @Autowired
    private SchoolRepository schoolRepository;

    public Map<String, Object> getSchoolDetails(Integer schoolCode, List<String> properties) {
        Optional<School> schoolOptional = schoolRepository.findById(schoolCode);
        if (schoolOptional.isEmpty()) {
            return null;
        }

        School school = schoolOptional.get();
        Map<String, Object> result = new HashMap<>();

        if (properties == null || properties.isEmpty()) {
            // Return all properties if no specific properties are requested
            for (Field field : School.class.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    result.put(field.getName(), field.get(school));
                } catch (IllegalAccessException e) {
                    // Log the error and continue
                    e.printStackTrace();
                }
            }
        } else {
            for (String property : properties) {
                try {
                    Field field = School.class.getDeclaredField(property);
                    field.setAccessible(true);
                    result.put(property, field.get(school));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Log the error and continue
                    e.printStackTrace();
                }
            }
        }

        return result;
    }
}
