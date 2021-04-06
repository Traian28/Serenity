package com.tnt.serenity.processor.impl;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.tnt.serenity.model.EventParameterKeys;
import com.tnt.serenity.model.EventTask;
import com.tnt.serenity.output.FileOutputer;
import com.tnt.serenity.processor.TaskProcessor;
import com.tnt.serenity.service.Task;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.metrics.annotation.Metered;

@Slf4j
@Singleton
public class JsonTaskProcessor implements TaskProcessor {

	private final ObjectMapper mapper;

	private final FileOutputer outputer;

	@Inject
	public JsonTaskProcessor(final ObjectMapper mapper, final FileOutputer outputer) {
		super();
		this.mapper = mapper;
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
			final DateTime serverTimestamp = new DateTime(eventTask.getEvent().getParameters().get(EventParameterKeys.SERVER_TIMESTAMP_KEY)).withZone(DateTimeZone.UTC);
			final String type = eventTask.getEvent().getParameters().get(EventParameterKeys.TYPE_KEY).toString().toLowerCase();
			final String platform = eventTask.getEvent().getParameters().get(EventParameterKeys.PLATFORM_KEY).toString().toLowerCase();
			
			final String key = String.format("%s/events/%s/%s/%s/json/%04d/%02d/%02d/%02d", gameId, type,
					eventName, platform, timestamp.getYear(), timestamp.getMonthOfYear(), timestamp.getDayOfMonth(),
					timestamp.getHourOfDay());
			
			eventTask.getEvent().getParameters().put(EventParameterKeys.TIMESTAMP_KEY, timestamp.getMillis());
			eventTask.getEvent().getParameters().put(EventParameterKeys.SERVER_TIMESTAMP_KEY, serverTimestamp.getMillis());

			final String output = this.mapper.writeValueAsString(eventTask.getEvent());
			
			this.outputer.write(key, output);

		} catch (final Exception e) {
			log.error("Could not parse task", e);
		}
	}
}
