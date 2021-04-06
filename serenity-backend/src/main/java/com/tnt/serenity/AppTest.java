package com.tnt.serenity;

import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;

import com.tnt.serenity.model.Event;
import com.tnt.serenity.rest.v1.viewmodel.EventLogViewModel;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class AppTest {

	public static void main(final String[] args) {

		final ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		final Client client = Client.create(config);

		final ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> of("timestamp", DateTime.now()
				.minusDays(2).getMillis());

		final ImmutableMap<String, Object> yesterdayProperties = ImmutableMap.<String, Object> of("timestamp", DateTime
				.now().minusDays(1).getMillis());

		final EventLogViewModel model = new EventLogViewModel(Arrays.asList(new Event("Logged In", properties),
				new Event("Logged Out", yesterdayProperties)));
		final EventLogViewModel nowModel = new EventLogViewModel(Arrays.asList(new Event("Logged In", ImmutableMap
				.<String, Object> of())));

		for (int i = 0; i < 20; i++) {
			client.resource("http://localhost:8080/rest/v1/games/pogs/eventlog").type(MediaType.APPLICATION_JSON)
					.post(ClientResponse.class, model);
		}

		for (int i = 0; i < 20; i++) {
			client.resource("http://localhost:8080/rest/v1/games/pogs/eventlog").type(MediaType.APPLICATION_JSON)
					.post(ClientResponse.class, nowModel);
		}

	}

}
