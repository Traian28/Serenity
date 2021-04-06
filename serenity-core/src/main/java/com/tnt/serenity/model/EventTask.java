package com.tnt.serenity.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EventTask {

	private final String gameId;

	private final Event event;

	public EventTask() {
		this(null, null);
	}

}
