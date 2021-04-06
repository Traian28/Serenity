package com.tnt.serenity.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Event {

	private final String name;

	private Map<String, Object> parameters;

	public Event() {
		this(null, null);
	}

}
