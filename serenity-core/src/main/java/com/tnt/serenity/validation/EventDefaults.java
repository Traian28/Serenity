package com.tnt.serenity.validation;

import java.util.HashMap;

import com.tnt.serenity.model.EventParameterKeys;
import org.joda.time.DateTime;

import com.tnt.serenity.model.Event;
import com.ea.tnt.util.DateTimeUtils;

public class EventDefaults {

	private static final String GAME_ID_KEY = "gameId";

	public Event populateDefaults(final String gameId, final Event event) {

		if (event.getParameters() == null) {
			event.setParameters(new HashMap<String, Object>());
		}
		
		if (event.getParameters().containsKey(EventParameterKeys.TIMESTAMP_KEY) && event.getParameters().get(EventParameterKeys.TIMESTAMP_KEY) instanceof String) {
			DateTime timestamp = DateTime.parse((String) event.getParameters().get(EventParameterKeys.TIMESTAMP_KEY));
			event.getParameters().put(EventParameterKeys.TIMESTAMP_KEY, timestamp.getMillis());
		}

		final Long serverTimestamp = new DateTimeUtils().nowUtc().getMillis();

		// Server Timestamp
		event.getParameters().put(EventParameterKeys.SERVER_TIMESTAMP_KEY, serverTimestamp);

		// Game Id
		event.getParameters().put(GAME_ID_KEY, gameId);

		return event;

	}

}
