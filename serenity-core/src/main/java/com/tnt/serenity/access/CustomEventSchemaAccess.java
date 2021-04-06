package com.tnt.serenity.access;

import com.ea.tnt.rest.ListWithCursor;
import com.tnt.serenity.model.CustomEventSchema;

public interface CustomEventSchemaAccess {
	
	public String nextCustomEventSchemaId();
	
	public boolean hasCustomEventSchemaId(String id);
	
	public boolean hasGameIdAndEventName(String gameId, String eventName);

	public void save(CustomEventSchema schema);
	
	public CustomEventSchema getSchemaById(String id);
	
	public CustomEventSchema getSchemaByGameIdAndEventName(String gameId, String eventName);

	public ListWithCursor<CustomEventSchema> listSchemaByGameId(final String gameId);

	public ListWithCursor<CustomEventSchema> listSchemaByGameId(final String gameId, final String cursor);
	
	public CustomEventSchema update(CustomEventSchema schema);
	
	public void delete(CustomEventSchema schema);
}
