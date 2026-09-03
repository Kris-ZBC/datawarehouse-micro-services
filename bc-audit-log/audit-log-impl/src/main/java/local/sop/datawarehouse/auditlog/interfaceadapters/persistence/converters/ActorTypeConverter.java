package local.sop.datawarehouse.auditlog.interfaceadapters.persistence.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import local.sop.datawarehouse.sharedlib.enums.ActorType;

@Converter(autoApply = false)
public class ActorTypeConverter implements AttributeConverter<ActorType, String> {

    @Override
    public String convertToDatabaseColumn(ActorType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public ActorType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ActorType.valueOf(dbData.toUpperCase());
    }
}
