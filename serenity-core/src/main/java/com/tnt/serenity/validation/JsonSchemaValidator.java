package com.tnt.serenity.validation;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class JsonSchemaValidator {

	private final JsonValidator validator;

	private static final String JSON_SCHEMA = getJsonSchema();

	@Inject
	public JsonSchemaValidator(final JsonValidator validator) {
		this.validator = validator;
	}

	public ValidationResult validate(final String jsonSchemaToValidate) {

		final ValidationResult result = this.validator.validate(JSON_SCHEMA, jsonSchemaToValidate);
		return result;

	}

	private static String getJsonSchema() {

		String jsonSchema = "";

		try {
			final InputStream stream = JsonSchemaValidator.class.getClassLoader().getResourceAsStream("schema-validation.json");
			jsonSchema = IOUtils.toString(stream);
		} catch (IOException e) {
			log.error("Failed to look up json schema validator from resources", e);
		}

		return jsonSchema;
	}
}
