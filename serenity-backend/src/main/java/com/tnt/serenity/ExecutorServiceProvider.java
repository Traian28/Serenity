package com.tnt.serenity;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class ExecutorServiceProvider implements Provider<ExecutorService> {

	private final Integer numThreads;

	@Inject
	public ExecutorServiceProvider(@Named("num.threads") final Integer numThreads) {
		super();
		this.numThreads = numThreads;
	}

	@Override
	public ExecutorService get() {
		final RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
		return new ThreadPoolExecutor(this.numThreads, this.numThreads, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(10000), rejectedExecutionHandler);
	}

}
