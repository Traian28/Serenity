package com.tnt.serenity;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Sets;
import com.tnt.serenity.processor.TaskProcessor;
import com.tnt.serenity.service.QueueService;
import com.tnt.serenity.service.Task;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tnt.serenity.model.EventTask;
import com.google.common.collect.Lists;

public class TaskRunnableTest {

	private static final String PAYLOAD = "some-payload";

	private static final String HANDLE = UUID.randomUUID().toString();

	private static final Task TASK = new Task(HANDLE, PAYLOAD);

	private static final List<Task> TASKS = Lists.newArrayList(TASK);

	@Mock
	private QueueService<EventTask> mockQueue;

	@Mock
	private TaskProcessor mockProcessor;

	private TaskRunnable runnable;

	@BeforeMethod(groups = "automated")
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.runnable = new TaskRunnable(this.mockQueue, Sets.newHashSet(this.mockProcessor));
	}

	public void verify() {
		Mockito.verifyNoMoreInteractions(this.mockProcessor, this.mockQueue);
	}

	@Test(groups = "automated")
	public void testRun() {
		Mockito.when(this.mockQueue.lease()).thenReturn(TASKS);
		this.runnable.run();

		Mockito.verify(this.mockQueue).lease();
		Mockito.verify(this.mockProcessor).process(TASK);
		Mockito.verify(this.mockQueue).delete(TASKS);

		this.verify();
	}

	@Test(groups = "automated")
	public void testRunNoTasks() {
		this.runnable.run();
		Mockito.verify(this.mockQueue).lease();
		this.verify();
	}
}
