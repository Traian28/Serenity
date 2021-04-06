package com.tnt.serenity.access.dynamo;

import java.util.List;
import java.util.UUID;

import com.tnt.serenity.access.dynamo.model.EventGroupDTO;
import com.tnt.serenity.exception.DataNotFoundException;
import com.tnt.serenity.model.EventGroup;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DynamoEventGroupAccessDatabaseTest extends DynamoAccessBaseDatabaseTest<EventGroupDTO> {

	private final DynamoEventGroupAccess access;

	public DynamoEventGroupAccessDatabaseTest() {
		super(EventGroupDTO.class);
		this.access = new DynamoEventGroupAccess(this.dynamoMapper, this.mapperFactory.getMapperFacade());
	}

	@Test(groups = "database")
	public void test() {

		// Create
		final String gameId = UUID.randomUUID().toString();
		final String eventGroupName = UUID.randomUUID().toString();
		final EventGroup eventGroup = new EventGroup().withName(eventGroupName);

		final EventGroup createEventGroup = this.access.create(gameId, eventGroup);

		// Get
		final EventGroup getEventGroup = this.access.get(gameId, createEventGroup.getId());
		final EventGroup getListEventGroup = this.access.getList(gameId).get(0);

		Assert.assertEquals(getEventGroup, createEventGroup);
		Assert.assertEquals(getListEventGroup, createEventGroup);

		// Update
		final String newEventGroupName = UUID.randomUUID().toString();
		final EventGroup updateEventGroup = this.access.update(gameId, createEventGroup.getId(), eventGroup.withName(newEventGroupName));

		Assert.assertEquals(updateEventGroup.getName(), newEventGroupName);

		// Get
		final EventGroup getEventGroup2 = this.access.get(gameId, createEventGroup.getId());
		final EventGroup getListEventGroup2 = this.access.getList(gameId).get(0);

		Assert.assertEquals(getEventGroup2, updateEventGroup);
		Assert.assertEquals(getListEventGroup2, updateEventGroup);

		// Delete
		this.access.delete(gameId, createEventGroup.getId());

		try {
			this.access.get(gameId, createEventGroup.getId());
			Assert.fail("Expected DataNotFoundException");
		} catch (final DataNotFoundException e) {

		}

		try {
			this.access.update(gameId, createEventGroup.getId(), eventGroup.withName(newEventGroupName));
			Assert.fail("Expected DataNotFoundException");
		} catch (final DataNotFoundException e) {

		}

		try {
			this.access.delete(gameId, createEventGroup.getId());
			Assert.fail("Expected DataNotFoundException");
		} catch (final DataNotFoundException e) {

		}

		final List<EventGroup> groups = this.access.getList(gameId);
		Assert.assertTrue(groups.isEmpty());
	}

}
