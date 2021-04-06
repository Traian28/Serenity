package com.tnt.serenity.processor.impl;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.tnt.serenity.ObjectMapperProvider;
import com.tnt.serenity.model.Event;
import com.tnt.serenity.model.EventTask;
import com.tnt.serenity.output.FileOutputer;
import com.tnt.serenity.service.Task;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.common.collect.ImmutableMap;
import com.tnt.serenity.model.CsvAdEvent;

public class CsvTaskProcessorTest {
	
	private ObjectMapper mapper;
	
	private CsvMapper csvMapper;

	private CsvTaskProcessor processor;

	private FileOutputer mockFileOutputer;

	@BeforeMethod(groups = "automated")
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.mapper = new ObjectMapperProvider().getContext(JsonTaskProcessorTest.class);
		this.csvMapper = new CsvMapper();
		this.processor = new CsvTaskProcessor(mapper, csvMapper, mockFileOutputer);
	}

	@Test(groups = "automated")
	public void test() throws JsonGenerationException, JsonMappingException, IOException {
		
		Map<String, Object> properties = ImmutableMap.<String, Object> of();
		String handle = UUID.randomUUID().toString();
		String name = "ad-shown";
		String gameId = UUID.randomUUID().toString();
		Event event = new Event(name, properties);
		EventTask etask = new EventTask(gameId, event);
		String payload = this.mapper.writeValueAsString(etask);
		
		System.out.println("Schema:" + CsvAdEvent.getCsvSchema().getColumnDesc());
		
		Task task = new Task(handle, payload);
		this.processor.process(task);
	}

}
