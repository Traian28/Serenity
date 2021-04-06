package com.tnt.serenity.processor.impl;

import java.util.Map;
import java.util.UUID;

import com.tnt.serenity.ObjectMapperProvider;
import com.tnt.serenity.output.FileOutputer;
import com.tnt.serenity.service.Task;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tnt.serenity.model.Event;
import com.tnt.serenity.model.EventTask;
import com.google.common.collect.ImmutableMap;

public class JsonTaskProcessorTest {

	private ObjectMapper mapper;

	private static final String HANDLE = UUID.randomUUID().toString();

	private static final String NAME = "ad-shown";

	private static final Map<String, Object> PROPERTIES = ImmutableMap.<String, Object> of();

	private static final Event EVENT = new Event(NAME, PROPERTIES);

	private static final String GAME_ID = UUID.randomUUID().toString();

	private static final EventTask EVENT_TASK = new EventTask(GAME_ID, EVENT);

	private static final String ENVIRONMENT_NAME = "dev";

	private String payload;

	private Task task;

	private JsonTaskProcessor processor;

	private FileOutputer mockFileOutputer;

	@BeforeMethod(groups = "automated")
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.mapper = new ObjectMapperProvider().getContext(JsonTaskProcessorTest.class);
		this.payload = this.mapper.writeValueAsString(EVENT_TASK);
		this.task = new Task(HANDLE, this.payload);
		this.processor = new JsonTaskProcessor(this.mapper, this.mockFileOutputer);
	}

	@Test(groups = "automated")
	public void test() {
		this.processor.process(this.task);
	}

}
