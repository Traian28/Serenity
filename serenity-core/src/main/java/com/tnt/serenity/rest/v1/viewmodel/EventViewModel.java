package com.tnt.serenity.rest.v1.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.tnt.serenity.model.Event;

@AllArgsConstructor
@Data
public class EventViewModel {

	private final Event event;

	public EventViewModel() {
		this(null);
	}

}
