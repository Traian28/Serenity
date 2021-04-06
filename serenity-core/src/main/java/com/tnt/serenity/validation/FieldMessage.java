package com.tnt.serenity.validation;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FieldMessage {

	private final String fieldName;

	private final Level level;

	private final String message;

	public FieldMessage(Level level, String message) {
		this("", level, message);
	}

	public enum Level {
		INFO,
		WARN,
		ERROR
	}

}
