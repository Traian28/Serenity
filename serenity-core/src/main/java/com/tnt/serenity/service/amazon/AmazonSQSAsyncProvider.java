package com.tnt.serenity.service.amazon;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class AmazonSQSAsyncProvider implements Provider<AmazonSQSAsync> {

	private final String accessKey;

	private final String secretKey;

	@Inject
	public AmazonSQSAsyncProvider(@Named("aws.sqs.access.key") final String accessKey,
			@Named("aws.sqs.secret.key") final String secretKey) {
		super();
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	@Override
	public AmazonSQSAsync get() {
		final AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		final AmazonSQSAsync client = new AmazonSQSAsyncClient(credentials);
		return new AmazonSQSBufferedAsyncClient(client);
	}

}
