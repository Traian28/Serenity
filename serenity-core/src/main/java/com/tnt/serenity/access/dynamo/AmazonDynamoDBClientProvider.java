package com.tnt.serenity.access.dynamo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class AmazonDynamoDBClientProvider implements Provider<AmazonDynamoDBClient> {

	private final String accessKey;

	private final String secretKey;
	
	private final String endpoint;

	@Inject
	public AmazonDynamoDBClientProvider(@Named("aws.dynamo.access.key") final String accessKey,
			@Named("aws.dynamo.secret.key") final String secretKey,
			@Named("aws.dynamo.endpoint") final String endpoint) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.endpoint = endpoint;
	}

	@Override
	public AmazonDynamoDBClient get() {
		final AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		if (endpoint != null && !endpoint.isEmpty()) {
			client.setEndpoint(endpoint);
		}
		return client;
	}

}
