package com.example.security.data;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Converter
public class EncryptedFieldConverter implements AttributeConverter<String, String> {

    private static DataSecurityUtil dataSecurityUtil;

    // We use a static setter to allow JPA to access the Spring bean.
    // In a pure Spring environment, @Configurable can be used, but this is a simpler workaround for demo purposes.
    @Autowired
    public void setDataSecurityUtil(DataSecurityUtil util) {
        EncryptedFieldConverter.dataSecurityUtil = util;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (!StringUtils.hasText(attribute)) {
            return attribute;
        }
        if (dataSecurityUtil == null) {
             throw new IllegalStateException("DataSecurityUtil has not been initialized. Ensure Spring context is loaded.");
        }
        return dataSecurityUtil.encryptField(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return dbData;
        }
        if (dataSecurityUtil == null) {
            throw new IllegalStateException("DataSecurityUtil has not been initialized. Ensure Spring context is loaded.");
        }
        return dataSecurityUtil.decryptField(dbData);
    }
}
