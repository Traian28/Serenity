package com.tnt.serenity.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CustomEventSchema {

	private final String id;

	private final String gameId;

	private final String eventName;

	private final String jsonSchema;
	
}
