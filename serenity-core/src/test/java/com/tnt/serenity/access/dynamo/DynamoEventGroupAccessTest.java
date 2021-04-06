package com.tnt.serenity.access.dynamo;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.tnt.serenity.access.dynamo.model.EventGroupDTO;
import com.tnt.serenity.exception.DataNotFoundException;
import com.tnt.serenity.model.EventGroup;
import ma.glasnost.orika.MapperFacade;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

public class DynamoEventGroupAccessTest {

	private static final String GAME_ID = UUID.randomUUID().toString();

	private static final String EVENT_GROUP_ID = UUID.randomUUID().toString();

	private static final String EVENT_NAME = UUID.randomUUID().toString();

	private static final EventGroup EVENT_GROUP = new EventGroup(EVENT_GROUP_ID, GAME_ID, EVENT_NAME);

	private static final EventGroupDTO DYNAMO_EVENT_GROUP = new EventGroupDTO(EVENT_GROUP_ID, GAME_ID, EVENT_NAME);

	private DynamoEventGroupAccess access;

	@Mock
	private DynamoDBMapper mockDynamoMapper;

	@Mock
	private MapperFacade mockMapperFacade;

	@Mock
	private PaginatedQueryList<EventGroupDTO> mockQueryList;

	@SuppressWarnings("unchecked")
	@BeforeMethod(groups = "automated")
	public void setup() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(this.mockDynamoMapper.query(Matchers.eq(EventGroupDTO.class), Matchers.any(DynamoDBQueryExpression.class))).thenReturn(
				this.mockQueryList);
		Mockito.when(this.mockDynamoMapper.load(EventGroupDTO.class, EVENT_GROUP_ID)).thenReturn(DYNAMO_EVENT_GROUP);

		Mockito.when(this.mockMapperFacade.mapAsList(this.mockQueryList, EventGroup.class)).thenReturn(Arrays.asList(EVENT_GROUP));
		Mockito.when(this.mockMapperFacade.map(Matchers.any(EventGroupDTO.class), Matchers.eq(EventGroup.class))).thenReturn(EVENT_GROUP);
		Mockito.when(this.mockMapperFacade.map(EVENT_GROUP, EventGroupDTO.class)).thenReturn(DYNAMO_EVENT_GROUP);

		this.access = new DynamoEventGroupAccess(this.mockDynamoMapper, this.mockMapperFacade);
	}

	public void verify() {
		Mockito.verifyNoMoreInteractions(this.mockDynamoMapper, this.mockMapperFacade, this.mockQueryList);
	}

	@SuppressWarnings("unchecked")
	@Test(groups = "automated")
	public void testGetList() {
		final List<EventGroup> groups = this.access.getList(GAME_ID);

		Assert.assertEquals(groups, Arrays.asList(EVENT_GROUP));

		Mockito.verify(this.mockDynamoMapper).query(Matchers.eq(EventGroupDTO.class), Matchers.any(DynamoDBQueryExpression.class));
		Mockito.verify(this.mockMapperFacade).mapAsList(this.mockQueryList, EventGroup.class);

		this.verify();
	}

	@Test(groups = "automated")
	public void testGet() {
		final EventGroup group = this.access.get(GAME_ID, EVENT_GROUP_ID);

		Assert.assertEquals(group, EVENT_GROUP);

		Mockito.verify(this.mockDynamoMapper).load(EventGroupDTO.class, EVENT_GROUP_ID);
		Mockito.verify(this.mockMapperFacade).map(DYNAMO_EVENT_GROUP, EventGroup.class);

		this.verify();
	}

	@Test(groups = "automated")
	public void testGetNotFound() {

		Mockito.when(this.mockDynamoMapper.load(EventGroupDTO.class, EVENT_GROUP_ID)).thenReturn(null);

		try {
			this.access.get(GAME_ID, EVENT_GROUP_ID);
			Assert.fail("Expected DataNotFoundException");
		} catch (final DataNotFoundException e) {

		}

		Mockito.verify(this.mockDynamoMapper).load(EventGroupDTO.class, EVENT_GROUP_ID);

		this.verify();
	}

	@Test(groups = "automated")
	public void testCreate() {
		final EventGroup group = this.access.create(GAME_ID, EVENT_GROUP);

		Assert.assertEquals(group, EVENT_GROUP);

		Mockito.verify(this.mockMapperFacade).map(EVENT_GROUP, EventGroupDTO.class);
		Mockito.verify(this.mockDynamoMapper).save(Matchers.any(EventGroupDTO.class));
		Mockito.verify(this.mockMapperFacade).map(Matchers.any(EventGroupDTO.class), Matchers.eq(EventGroup.class));

		this.verify();

	}

	@Test(groups = "automated")
	public void testUpdate() {

		final EventGroup group = this.access.update(GAME_ID, EVENT_GROUP_ID, EVENT_GROUP);

		Assert.assertEquals(group, EVENT_GROUP);

		Mockito.verify(this.mockDynamoMapper).load(EventGroupDTO.class, EVENT_GROUP_ID);
		Mockito.verify(this.mockMapperFacade).map(EVENT_GROUP, DYNAMO_EVENT_GROUP);
		Mockito.verify(this.mockDynamoMapper).save(Matchers.any(EventGroupDTO.class));
		Mockito.verify(this.mockMapperFacade).map(Matchers.any(EventGroupDTO.class), Matchers.eq(EventGroup.class));

		this.verify();
	}

	@Test(groups = "automated")
	public void testUpdateNotFound() {

		Mockito.when(this.mockDynamoMapper.load(EventGroupDTO.class, EVENT_GROUP_ID)).thenReturn(null);

		try {
			this.access.update(GAME_ID, EVENT_GROUP_ID, EVENT_GROUP);
			Assert.fail("Expected DataNotFoundException");
		} catch (final DataNotFoundException e) {

		}

		Mockito.verify(this.mockDynamoMapper).load(EventGroupDTO.class, EVENT_GROUP_ID);

		this.verify();
	}

	@Test(groups = "automated")
	public void testDelete() {
		this.access.delete(GAME_ID, EVENT_GROUP_ID);
		Mockito.verify(this.mockDynamoMapper).load(EventGroupDTO.class, EVENT_GROUP_ID);
		Mockito.verify(this.mockDynamoMapper).delete(DYNAMO_EVENT_GROUP);
	}

	@Test(groups = "automated")
	public void testDeleteNotFound() {

		Mockito.when(this.mockDynamoMapper.load(EventGroupDTO.class, EVENT_GROUP_ID)).thenReturn(null);

		try {
			this.access.delete(GAME_ID, EVENT_GROUP_ID);
			Assert.fail("Expected DataNotFoundException");
		} catch (final DataNotFoundException e) {

		}

		Mockito.verify(this.mockDynamoMapper).load(EventGroupDTO.class, EVENT_GROUP_ID);

		this.verify();
	}

}
