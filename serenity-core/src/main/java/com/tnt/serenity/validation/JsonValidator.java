package com.tnt.serenity.validation;

import com.tnt.serenity.exception.ValidationFailedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class JsonValidator {

	private final JsonSchemaFactory jsonSchemaFactory;

	@Inject
	public JsonValidator(JsonSchemaFactory jsonSchemaFactory) {
		this.jsonSchemaFactory = jsonSchemaFactory;
	}

	public ValidationResult validate(final String jsonSchema, final String jsonToValidate) {

		try {

			final JsonNode jsonSchemaNode = JsonLoader.fromString(jsonSchema);
			final JsonNode jsonToValidateNode = JsonLoader.fromString(jsonToValidate);

			final JsonSchema validationSchema = this.jsonSchemaFactory.getJsonSchema(jsonSchemaNode);

			final ProcessingReport report = validationSchema.validate(jsonToValidateNode);

			final List<FieldMessage> fieldMessages = this.convertProcessingReportToFieldMessages(report);

			return new ValidationResult(fieldMessages);

		} catch (IOException | ProcessingException e) {
			log.error("Failed to validate json with json schema", e);
			throw new ValidationFailedException("Failed to validate event json", e);
		}

	}

	private List<FieldMessage> convertProcessingReportToFieldMessages(ProcessingReport report) {
		List<FieldMessage> errors = Lists.newArrayList();

		for (final Iterator<ProcessingMessage> iter = report.iterator(); iter.hasNext(); ) {
			final ProcessingMessage message = iter.next();
			final FieldMessage.Level level = getFieldMessageLevel(message);
			errors.add(new FieldMessage(level, message.getMessage()));
		}

		return errors;
	}

	private FieldMessage.Level getFieldMessageLevel(final ProcessingMessage message) {
		if (LogLevel.INFO == message.getLogLevel()) {
			return FieldMessage.Level.INFO;
		} else if (LogLevel.WARNING == message.getLogLevel()) {
			return FieldMessage.Level.WARN;
		} else {
			return FieldMessage.Level.ERROR;
		}
	}
}
