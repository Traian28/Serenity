package com.tnt.serenity.validation;

import com.tnt.serenity.service.CustomEventSchemaService;
import com.tnt.serenity.exception.ValidationFailedException;
import com.tnt.serenity.model.CustomEventSchema;
import com.tnt.serenity.model.Event;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

@Slf4j
public class CustomEventValidator {

	private final CustomEventSchemaService service;

	private final ObjectMapper mapper;

	private final JsonValidator jsonValidator;

	@Inject
	public CustomEventValidator(final CustomEventSchemaService service, final ObjectMapper mapper, final JsonValidator jsonValidator) {
		this.service = service;
		this.mapper = mapper;
		this.jsonValidator = jsonValidator;
	}

	public ValidationResult validate(final String gameId, final Event event) {

		final CustomEventSchema customEventSchema = this.service.getSchemaByGameIdAndEventName(gameId, event.getName());

		try {
			final String eventJson = this.mapper.writeValueAsString(event);
			final ValidationResult result = this.jsonValidator.validate(customEventSchema.getJsonSchema(), eventJson);
			return result;
		} catch (IOException e) {
			log.error("Failed to validate event json", e);
			throw new ValidationFailedException("Failed to validate event json", e);
		}

	}

}
