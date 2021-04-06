package com.tnt.serenity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExecutorServiceProviderTest {

	private static final Integer NUM_THREADS = 10;

	private ExecutorServiceProvider provider;

	@BeforeMethod(groups = "automated")
	public void setup() {
		this.provider = new ExecutorServiceProvider(NUM_THREADS);
	}

	@Test(groups = "automated")
	public void testGet() {
		final ExecutorService executorService = this.provider.get();
		Assert.assertNotNull(executorService);
		Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
	}

}
