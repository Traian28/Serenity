package com.tnt.serenity.access.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class DynamoDBMapperProvider implements Provider<DynamoDBMapper> {

	private final AmazonDynamoDBClient client;

	private final String tablePrefix;

	@Inject
	public DynamoDBMapperProvider(final AmazonDynamoDBClient client,
			@Named("aws.dynamo.table.name.prefix") final String tablePrefix) {
		super();
		this.client = client;
		this.tablePrefix = tablePrefix;
	}

	@Override
	public DynamoDBMapper get() {
		final DynamoDBMapperConfig config = new DynamoDBMapperConfig(
				TableNameOverride.withTableNamePrefix(this.tablePrefix));
		return new DynamoDBMapper(this.client, config);
	}

}
