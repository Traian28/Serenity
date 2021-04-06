package com.tnt.serenity;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.tnt.serenity.model.EventTask;
import com.tnt.serenity.processor.TaskProcessor;
import com.tnt.serenity.service.QueueService;
import lombok.extern.slf4j.Slf4j;

import com.google.inject.Inject;

@Slf4j
public class SerenityBackend {

	private final QueueService<EventTask> queue;

	private final Set<TaskProcessor> processors;

	private final ExecutorService service;

	@Inject
	public SerenityBackend(final QueueService<EventTask> queue, final Set<TaskProcessor> processors,
			final ExecutorService service) {
		super();
		this.queue = queue;
		this.processors = processors;
		this.service = service;
	}

	public void run() {

		Runtime.getRuntime().addShutdownHook(new ShutdownThread(this.service));

		log.info("Process started...");

		while (true) {
			this.service.execute(new TaskRunnable(this.queue, this.processors));
		}
	}

	@Slf4j
	private static class ShutdownThread extends Thread {

		private final ExecutorService service;

		public ShutdownThread(final ExecutorService service) {
			super();
			this.service = service;
		}

		@Override
		public void run() {

			log.info("Shutting down service.");
			this.service.shutdown();

			try {
				this.service.awaitTermination(10, TimeUnit.SECONDS);

				if (this.service.isShutdown()) {
					this.service.shutdownNow();
				}

			} catch (final InterruptedException e) {
				log.error("Failed clean shut down.");

				if (this.service.isShutdown()) {
					this.service.shutdownNow();
				}
			}

			log.info("Bye.");
		}
	}
}
