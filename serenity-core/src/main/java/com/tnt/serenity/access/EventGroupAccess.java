package com.tnt.serenity.access;

import java.util.List;

import com.tnt.serenity.model.EventGroup;

public interface EventGroupAccess {

	List<EventGroup> getList(final String gameId);

	EventGroup get(final String gameId, final String eventGroupId);

	EventGroup create(final String gameId, final EventGroup eventGroup);

	EventGroup update(final String gameId, final String eventGroupId, final EventGroup eventGroup);

	void delete(final String gameId, final String eventGroupId);

}
