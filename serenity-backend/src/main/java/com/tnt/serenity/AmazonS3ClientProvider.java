package com.tnt.serenity;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class AmazonS3ClientProvider implements Provider<AmazonS3Client> {

	private final String accessKey;

	private final String secretKey;

	@Inject
	public AmazonS3ClientProvider(@Named("aws.s3.access.key") final String accessKey,
			@Named("aws.s3.secret.key") final String secretKey) {
		super();
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	@Override
	public AmazonS3Client get() {
		final BasicAWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		return new AmazonS3Client(credentials);
	}

}
