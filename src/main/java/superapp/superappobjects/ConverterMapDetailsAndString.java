package superapp.superappobjects;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;

public class ConverterMapDetailsAndString  implements AttributeConverter<Map<String, Object>, String>{
	private ObjectMapper jackson;
	
	public ConverterMapDetailsAndString(ObjectMapper jackson) {
		this.jackson = jackson;
	}

	@Override
	public String convertToDatabaseColumn(Map<String, Object> attribute) {
		try {
			return this.jackson
				.writeValueAsString(attribute);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, Object> convertToEntityAttribute(String dbData) {
		try {
			return this.jackson
				.readValue(dbData, Map.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
