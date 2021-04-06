package com.tnt.serenity.access.dynamo;

import java.util.List;
import java.util.UUID;

import ma.glasnost.orika.MapperFacade;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.tnt.serenity.access.EventGroupAccess;
import com.tnt.serenity.access.dynamo.model.EventGroupDTO;
import com.tnt.serenity.exception.DataNotFoundException;
import com.tnt.serenity.model.EventGroup;
import com.google.inject.Inject;

public class DynamoEventGroupAccess implements EventGroupAccess {

	private final DynamoDBMapper dynamoMapper;
	private final MapperFacade mapperFacade;

	@Inject
	public DynamoEventGroupAccess(final DynamoDBMapper dynamoMapper, final MapperFacade mapperFacade) {
		super();
		this.dynamoMapper = dynamoMapper;
		this.mapperFacade = mapperFacade;
	}

	@Override
	public List<EventGroup> getList(final String gameId) {

		final DynamoDBQueryExpression<EventGroupDTO> queryExpression = new DynamoDBQueryExpression<EventGroupDTO>()
				.withHashKeyValues(new EventGroupDTO().withGameId(gameId)).withConsistentRead(false).withIndexName("gameId");

		final List<EventGroupDTO> eventGroupDtos = this.dynamoMapper.query(EventGroupDTO.class, queryExpression);
		final List<EventGroup> eventGroups = this.mapperFacade.mapAsList(eventGroupDtos, EventGroup.class);
		return eventGroups;
	}

	@Override
	public EventGroup get(final String gameId, final String eventGroupId) {
		final EventGroupDTO eventGroupDto = this.dynamoMapper.load(EventGroupDTO.class, eventGroupId);

		if (eventGroupDto == null) {
			throw new DataNotFoundException();
		}

		final EventGroup eventGroup = this.mapperFacade.map(eventGroupDto, EventGroup.class);
		return eventGroup;
	}

	@Override
	public EventGroup create(final String gameId, final EventGroup eventGroup) {
		final EventGroupDTO eventGroupDto = this.mapperFacade.map(eventGroup, EventGroupDTO.class).withGameId(gameId)
				.withId(UUID.randomUUID().toString());
		this.dynamoMapper.save(eventGroupDto);
		final EventGroup createdEventGroup = this.mapperFacade.map(eventGroupDto, EventGroup.class);
		return createdEventGroup;
	}

	@Override
	public EventGroup update(final String gameId, final String eventGroupId, final EventGroup eventGroup) {
		EventGroupDTO eventGroupDto = this.dynamoMapper.load(EventGroupDTO.class, eventGroupId);

		if (eventGroupDto == null) {
			throw new DataNotFoundException();
		}

		this.mapperFacade.map(eventGroup, eventGroupDto);
		eventGroupDto = eventGroupDto.withGameId(gameId).withId(eventGroupId);

		this.dynamoMapper.save(eventGroupDto);

		final EventGroup updatedEventGroup = this.mapperFacade.map(eventGroupDto, EventGroup.class);
		return updatedEventGroup;
	}

	@Override
	public void delete(final String gameId, final String eventGroupId) {
		final EventGroupDTO eventGroupDto = this.dynamoMapper.load(EventGroupDTO.class, eventGroupId);

		if (eventGroupDto == null) {
			throw new DataNotFoundException();
		}

		this.dynamoMapper.delete(eventGroupDto);
	}

}
