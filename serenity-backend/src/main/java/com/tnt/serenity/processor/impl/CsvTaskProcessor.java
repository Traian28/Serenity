package com.tnt.serenity.processor.impl;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.tnt.serenity.model.CsvAdEvent;
import com.tnt.serenity.model.EventParameterKeys;
import com.tnt.serenity.model.EventTask;
import com.tnt.serenity.output.FileOutputer;
import com.tnt.serenity.processor.TaskProcessor;
import com.tnt.serenity.service.Task;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.metrics.annotation.Metered;

@Slf4j
@Singleton
public class CsvTaskProcessor implements TaskProcessor {

	private final ObjectMapper mapper;

	private final CsvMapper csvMapper;

	private final CsvSchema schema;

	private final ObjectWriter writer;

	private final FileOutputer outputer;

	@Inject
	public CsvTaskProcessor(final ObjectMapper mapper, final CsvMapper csvMapper, final FileOutputer outputer) {
		super();
		this.mapper = mapper;
		this.csvMapper = csvMapper;
		this.schema = CsvAdEvent.getCsvSchema();
		this.writer = this.csvMapper.writer(this.schema);
		this.outputer = outputer;
	}

	@Override
	@Metered(rateUnit = TimeUnit.SECONDS)
	public void process(final Task task) {

		try {
			final EventTask eventTask = this.mapper.readValue(task.getPayload(), EventTask.class);

			final String gameId = eventTask.getGameId();
			final String eventName = eventTask.getEvent().getName();
			final DateTime timestamp = new DateTime(eventTask.getEvent().getParameters().get(EventParameterKeys.TIMESTAMP_KEY)).withZone(DateTimeZone.UTC);
			final DateTime serverTimestamp = new DateTime(eventTask.getEvent().getParameters().get(EventParameterKeys.SERVER_TIMESTAMP_KEY))
					.withZone(DateTimeZone.UTC);
			final String type = eventTask.getEvent().getParameters().get(EventParameterKeys.TYPE_KEY).toString().toLowerCase();

			if (!type.equals("tnt")) {
				return;
			}

			final String platform = eventTask.getEvent().getParameters().get(EventParameterKeys.PLATFORM_KEY).toString().toLowerCase();

			final String key = String.format("%s/events/%s/%s/%s/csv/%04d/%02d/%02d/%02d", gameId, type, eventName, platform, timestamp.getYear(),
					timestamp.getMonthOfYear(), timestamp.getDayOfMonth(), timestamp.getHourOfDay());

			eventTask.getEvent().getParameters().put(EventParameterKeys.TIMESTAMP_KEY, timestamp.getMillis());
			eventTask.getEvent().getParameters().put(EventParameterKeys.SERVER_TIMESTAMP_KEY, serverTimestamp.getMillis());

			final CsvAdEvent csvAdEvent = new CsvAdEvent(eventTask.getEvent());
			final String output = this.writer.writeValueAsString(csvAdEvent).trim().replace("\n", "").replace("\r", "");

			this.outputer.write(key, output);

		} catch (final Exception e) {
			log.error("Could not parse task", e);
		}
	}
}
