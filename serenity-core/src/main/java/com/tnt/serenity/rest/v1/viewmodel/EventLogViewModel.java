package com.tnt.serenity.rest.v1.viewmodel;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.tnt.serenity.model.Event;

@AllArgsConstructor
@Data
public class EventLogViewModel {

	private final List<Event> events;

	public EventLogViewModel() {
		this(null);
	}

}
