package com.tnt.serenity.service.amazon;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;

public class AmazonSQSAsyncProviderTest {

	private static final String ACCESS_KEY = UUID.randomUUID().toString();

	private static final String SECRET_KEY = UUID.randomUUID().toString();

	private AmazonSQSAsyncProvider provider;

	@BeforeMethod(groups = "automated")
	public void setup() {
		this.provider = new AmazonSQSAsyncProvider(ACCESS_KEY, SECRET_KEY);
	}

	@Test(groups = "automated")
	public void testGet() {
		final AmazonSQSAsync client = this.provider.get();
		Assert.assertTrue(client instanceof AmazonSQSBufferedAsyncClient);
	}

}
