package org.hypertrace.gateway.service.common.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.gateway.service.v1.common.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringToAttributeKindConverter extends ToAttributeKindConverter<String> {
  private static final Logger log = LoggerFactory.getLogger(StringToAttributeKindConverter.class);
  private static final TypeReference<Map<String, String>> mapOfString = new TypeReference<>() {};
  public static StringToAttributeKindConverter INSTANCE = new StringToAttributeKindConverter();
  private final ObjectMapper objectMapper = new ObjectMapper();

  private StringToAttributeKindConverter() {}

  public Value doConvert(String value, AttributeKind attributeKind, Value.Builder valueBuilder) {
    switch (attributeKind) {
      case TYPE_BOOL:
        valueBuilder.setValueType(ValueType.BOOL);
        valueBuilder.setBoolean(Boolean.valueOf(value));
        return valueBuilder.build();

      case TYPE_DOUBLE:
        valueBuilder.setValueType(ValueType.DOUBLE);
        valueBuilder.setDouble(Double.parseDouble(value));
        return valueBuilder.build();

      case TYPE_INT64:
        valueBuilder.setValueType(ValueType.LONG);
        // By default, aggregation is returned as String with Decimal-Value
        // parse as double first before converting to Long
        valueBuilder.setLong((long) Double.parseDouble(value));
        return valueBuilder.build();

      case TYPE_STRING:
        valueBuilder.setValueType(ValueType.STRING);
        valueBuilder.setString(value);
        return valueBuilder.build();

      case TYPE_TIMESTAMP:
        valueBuilder.setValueType(ValueType.TIMESTAMP);
        valueBuilder.setTimestamp(Long.parseLong(value));
        return valueBuilder.build();

      case TYPE_STRING_MAP:
        valueBuilder.setValueType(ValueType.STRING_MAP);
        valueBuilder.putAllStringMap(convertToMap(value));
        return valueBuilder.build();
      default:
        break;
    }
    return null;
  }

  private Map<String, String> convertToMap(String jsonString) {
    Map<String, String> mapData = new HashMap<>();
    try {
      if (!StringUtils.isEmpty(jsonString)) {
        mapData = objectMapper.readValue(jsonString, mapOfString);
      }
    } catch (IOException e) {
      log.warn("Unable to read Map JSON Strig data from: {}. With error: \n", jsonString, e);
      log.warn("Setting data as empty map instead");
    }
    return mapData;
  }
}
