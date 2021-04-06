package com.tnt.serenity.validation;

import com.google.api.client.util.Lists;
import lombok.Data;

import java.util.List;

@Data
public class ValidationResult {

	private final List<FieldMessage> fieldMessages;

	public ValidationResult() {
		this.fieldMessages = Lists.newArrayList();
	}

	public ValidationResult(final List<FieldMessage> fieldMessages) {
		this.fieldMessages = fieldMessages;
	}

	public ValidationResult(final ValidationResult... results) {
		this.fieldMessages = Lists.newArrayList();
		for (ValidationResult result : results) {
			this.fieldMessages.addAll(result.getFieldMessages());
		}
	}

	public boolean isValid() {
		return fieldMessages.isEmpty();
	}

}
