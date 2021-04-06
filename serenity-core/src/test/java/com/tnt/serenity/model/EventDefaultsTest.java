package com.tnt.serenity.model;

import java.util.UUID;

import com.tnt.serenity.validation.EventDefaults;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;

public class EventDefaultsTest {

	private static final String NAME = UUID.randomUUID().toString();
	
	private static final String GAME_ID = UUID.randomUUID().toString();

	private EventDefaults eventDefaults;

	@BeforeMethod(groups = "automated")
	public void setup() {
		this.eventDefaults = new EventDefaults();
	}

	@Test(groups = "automated")
	public void testNoProperties() {
		final Event event = new Event(NAME, null);
		Assert.assertNull(event.getParameters());

		this.eventDefaults.populateDefaults(GAME_ID, event);
		Assert.assertNotNull(event.getParameters());
	}
	
	@Test(groups = "automated")
	public void testServerTimestamp() {
		final Event event = new Event(NAME, Maps.<String, Object> newHashMap());
		Assert.assertFalse(event.getParameters().containsKey("serverTimestamp"));

		this.eventDefaults.populateDefaults(GAME_ID, event);
		Assert.assertTrue(event.getParameters().containsKey("serverTimestamp"));
	}

}
