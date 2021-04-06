package com.tnt.serenity.access.dynamo;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.ea.tnt.rest.ArrayListWithCursor;
import com.ea.tnt.rest.ListWithCursor;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import ma.glasnost.orika.MapperFacade;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.tnt.serenity.access.GuavaCustomEventSchemaAccess;
import com.tnt.serenity.access.dynamo.model.CustomEventSchemaDTO;
import com.tnt.serenity.exception.DataNotFoundException;
import com.tnt.serenity.model.CustomEventSchema;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

public class DynamoCustomEventSchemaAccess extends GuavaCustomEventSchemaAccess {

	private static final int RECORD_LIMIT = 20;

	private final DynamoDBMapper dynamoMapper;
	private final MapperFacade mapperFacade;
	
	@Inject
	public DynamoCustomEventSchemaAccess(final DynamoDBMapper dynamoMapper, MapperFacade mapperFacade) {
		this.dynamoMapper = dynamoMapper;
		this.mapperFacade = mapperFacade;
	}
	
	@Override
	public String nextCustomEventSchemaId() {
		return UUID.randomUUID().toString();
	}
	
	@Override
	public boolean hasCustomEventSchemaId(String id) {
		return (null != getSchemaById(id));
	}

	@Override
	public boolean hasGameIdAndEventName(String gameId, String eventName) {
		CustomEventSchemaDTO schemaDTO = getSchemaDTOByGameIdAndEventName(gameId, eventName);
		return (null != schemaDTO);
	}

	@Override
	public void save(CustomEventSchema schema) {
		update(schema);
	}
	
	@Override
	public CustomEventSchema doGetSchemaById(String id) {
		final CustomEventSchemaDTO schemaDTO = this.dynamoMapper.load(CustomEventSchemaDTO.class, id);
		if (schemaDTO == null) {
			throw new DataNotFoundException("No such EventSchema found, " + id);
		}
		CustomEventSchema schema = mapperFacade.map(schemaDTO, CustomEventSchema.class);
		return schema;
	}
	
	@Override
	public CustomEventSchema doGetSchemaByGameIdAndEventName(String gameId,
			String eventName) {
		CustomEventSchemaDTO schemaDTO = getSchemaDTOByGameIdAndEventName(gameId, eventName);
		if (schemaDTO == null) {
			throw new DataNotFoundException("No such EventSchema found, " + gameId + ", " + eventName);
		}
		CustomEventSchema schema = mapperFacade.map(schemaDTO, CustomEventSchema.class);
		return schema;
	}

	@Override
	public CustomEventSchema getSchemaByGameIdAndEventName(String gameId, String eventName) {
		return super.getSchemaByGameIdAndEventName(gameId, eventName);
	}

	@Override
	public ListWithCursor<CustomEventSchema> listSchemaByGameId(final String gameId) {
		return this.listSchemaByGameId(gameId, null);
	}

	@Override
	public ListWithCursor<CustomEventSchema> listSchemaByGameId(final String gameId, final String cursor) {

		final List<CustomEventSchema> eventSchemas = Lists.newArrayList();

		DynamoDBQueryExpression<CustomEventSchemaDTO> queryExpression = new DynamoDBQueryExpression<CustomEventSchemaDTO>().withHashKeyValues(new CustomEventSchemaDTO().withGameId(gameId))
				.withConsistentRead(false).withIndexName("gameId-eventName");

		if (StringUtils.isNotBlank(cursor)) {
			queryExpression = queryExpression.withExclusiveStartKey(ImmutableMap.of("id", new AttributeValue().withS(cursor),
					"gameId", new AttributeValue().withS(gameId)));
		}


		final PaginatedQueryList<CustomEventSchemaDTO> dtoList = this.dynamoMapper.query(CustomEventSchemaDTO.class, queryExpression);
		final Iterator<CustomEventSchemaDTO> iterator = dtoList.iterator();

		CustomEventSchema customEventSchema = null;
		int i = 0;

		while (iterator.hasNext() && i < RECORD_LIMIT) {
			customEventSchema = this.mapperFacade.map(iterator.next(), CustomEventSchema.class);
			eventSchemas.add(customEventSchema);
			i++;
		}

		final String newCursor = customEventSchema != null && iterator.hasNext() ? customEventSchema.getId() : null;

		return new ArrayListWithCursor<>(eventSchemas, newCursor);
	}
	
	private CustomEventSchemaDTO getSchemaDTOByGameIdAndEventName(String gameId, String eventName) {
		final DynamoDBQueryExpression<CustomEventSchemaDTO> query = getQueryForGetSchemaByEventNameAndGameId(eventName, gameId);
		List<CustomEventSchemaDTO> dtoList = this.dynamoMapper.query(CustomEventSchemaDTO.class, query);
		if (dtoList.size() > 0) {
			return dtoList.get(0);
		}
		return null;
	}
	
	private DynamoDBQueryExpression<CustomEventSchemaDTO> getQueryForGetSchemaByEventNameAndGameId(
			String eventName, String gameId) {
		return new DynamoDBQueryExpression<CustomEventSchemaDTO>()
				.withRangeKeyCondition(
						"eventName", new Condition().withComparisonOperator(
								ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(eventName)))
				.withHashKeyValues(new CustomEventSchemaDTO().withEventName(eventName).withGameId(gameId))
				.withConsistentRead(false)
				.withIndexName("gameId-eventName")
				.withLimit(1);
	}
	
	@Override
	public CustomEventSchema doUpdate(CustomEventSchema schema) {
		CustomEventSchemaDTO schemaDTO = mapperFacade.map(schema, CustomEventSchemaDTO.class);
		this.dynamoMapper.save(schemaDTO);
		return mapperFacade.map(schemaDTO, CustomEventSchema.class);
	}
	
	@Override
	public void doDelete(CustomEventSchema schema) {
		CustomEventSchemaDTO schemaDTO = mapperFacade.map(schema, CustomEventSchemaDTO.class);
		this.dynamoMapper.delete(schemaDTO);
	}

}
