package com.tnt.serenity;

import java.util.List;
import java.util.Set;

import com.tnt.serenity.model.EventTask;
import com.tnt.serenity.processor.TaskProcessor;
import com.tnt.serenity.service.QueueService;
import com.tnt.serenity.service.Task;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskRunnable implements Runnable {

	private final QueueService<EventTask> queue;

	private final Set<TaskProcessor> processors;

	public TaskRunnable(final QueueService<EventTask> queue, final Set<TaskProcessor> processors) {
		super();
		this.queue = queue;
		this.processors = processors;
	}

	@Override
	public void run() {

		final List<Task> tasks = this.queue.lease();

		if (!tasks.isEmpty()) {

			for (final Task task : tasks) {
				for (final TaskProcessor processor : this.processors) {
					processor.process(task);
				}
			}

			this.queue.delete(tasks);
		} else {
			log.debug("Leased 0 tasks.");
		}
	}
}
