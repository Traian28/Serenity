package com.tnt.serenity.service;

import com.ea.tnt.rest.ListWithCursor;
import com.tnt.serenity.access.CustomEventSchemaAccess;
import com.tnt.serenity.exception.DataDuplicateException;
import com.tnt.serenity.exception.DataNotFoundException;
import com.tnt.serenity.exception.InvalidCustomEventJsonSchemaException;
import com.tnt.serenity.model.CustomEventSchema;
import com.tnt.serenity.validation.JsonSchemaValidator;
import com.tnt.serenity.validation.ValidationResult;
import com.google.inject.Inject;

public class CustomEventSchemaService {
	
	private final CustomEventSchemaAccess customEventSchemaAccess;

	private final JsonSchemaValidator jsonSchemaValidator;
	
	@Inject
	public CustomEventSchemaService(CustomEventSchemaAccess customEventSchemaAccess,
									JsonSchemaValidator jsonSchemaValidator) {
		this.customEventSchemaAccess = customEventSchemaAccess;
		this.jsonSchemaValidator = jsonSchemaValidator;
	}

	public CustomEventSchema create(String gameId, String eventName, String jsonSchema) {

		verifyNoDuplicatedGameIdAndEventName(gameId, eventName);

		final ValidationResult report = this.jsonSchemaValidator.validate(jsonSchema);

		if (!report.isValid()) {
			throw new InvalidCustomEventJsonSchemaException(report);
		}

		final String id = customEventSchemaAccess.nextCustomEventSchemaId();
		final CustomEventSchema createdSchema = new CustomEventSchema(id, gameId, eventName, jsonSchema);

		customEventSchemaAccess.save(createdSchema);

		return createdSchema;
	}
	
	private void verifyNoDuplicatedGameIdAndEventName(String gameId, String eventName) {
		if (customEventSchemaAccess.hasGameIdAndEventName(gameId, eventName)) {
			throw new DataDuplicateException("Event name ," + eventName + "has already exist for game, " + gameId);
		}
	} 

	public CustomEventSchema getSchemaById(String id) {
		return customEventSchemaAccess.getSchemaById(id);
	}
	
	public CustomEventSchema getSchemaByGameIdAndEventName(String gameId, String eventName) {
		return customEventSchemaAccess.getSchemaByGameIdAndEventName(gameId, eventName);
	}

	public ListWithCursor<CustomEventSchema> listSchemaByGameId(String gameId, final String cursor) {
		return this.customEventSchemaAccess.listSchemaByGameId(gameId, cursor);
	}

	public ListWithCursor<CustomEventSchema> listSchemaByGameId(String gameId) {
			return customEventSchemaAccess.listSchemaByGameId(gameId);
	}
	
	public void update(CustomEventSchema schema) {

		final ValidationResult report = this.jsonSchemaValidator.validate(schema.getJsonSchema());

		if (!report.isValid()) {
			throw new InvalidCustomEventJsonSchemaException(report);
		}

		verifyCustomEventSchemaIdExist(schema.getId());
		verifyNoDuplicatedGameIdAndEventName(schema.getGameId(), schema.getEventName());
		customEventSchemaAccess.update(schema);
	}
	
	private void verifyCustomEventSchemaIdExist(String id) {
		if (!customEventSchemaAccess.hasCustomEventSchemaId(id)) {
			throw new DataNotFoundException(id);
		}
	}
	
	public void delete(CustomEventSchema schema) {
		customEventSchemaAccess.delete(schema);
	}
	
}
