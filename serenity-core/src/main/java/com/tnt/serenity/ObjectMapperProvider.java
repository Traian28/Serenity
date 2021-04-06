package com.tnt.serenity;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.ea.tnt.serialization.json.ISODateTimeModule;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	private static final ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new ISODateTimeModule());
		objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public ObjectMapper getContext(final Class<?> type) {
		return objectMapper;
	}
}
