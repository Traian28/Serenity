package com.tnt.serenity.service.amazon;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.tnt.serenity.exception.EnqueueFailedException;
import com.tnt.serenity.service.Task;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.tnt.serenity.ObjectMapperProvider;
import com.tnt.serenity.model.Event;
import com.tnt.serenity.model.EventTask;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class AmazonSimpleQueueServiceTest {

	private static final DateTime TIMESTAMP = DateTime.now().withZone(DateTimeZone.UTC);

	private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/893895740383/serenity-events-local";

	private static final Object USER_ID = UUID.randomUUID().toString();

	private static final Event EVENT = new Event("ad-shown", ImmutableMap.<String, Object> of("timestamp", TIMESTAMP,
			"userId", USER_ID));

	private static final String GAME_ID = UUID.randomUUID().toString();

	private static final EventTask EVENT_TASK = new EventTask(GAME_ID, EVENT);

	private static final String HANDLE = UUID.randomUUID().toString();

	private static final Task TASK = new Task(HANDLE, "");

	@Mock
	private AmazonSQSAsync mockClient;

	@Mock
	private ObjectMapper mockObjectMapper;

	private ObjectMapper objectMapper;

	private AmazonSimpleQueueService<EventTask> queueService;

	@BeforeMethod(groups = "automated")
	public void setup() {
		this.objectMapper = new ObjectMapperProvider().getContext(this.getClass());
		MockitoAnnotations.initMocks(this);
		this.queueService = new AmazonSimpleQueueService<>(this.mockClient, this.objectMapper, QUEUE_URL);
	}

	public void verify() {
		Mockito.verifyNoMoreInteractions(this.mockClient, this.mockObjectMapper);
	}

	@Test(groups = "automated")
	public void testEnqueue() throws Exception {
		this.queueService.enqueue(EVENT_TASK);
		Mockito.verify(this.mockClient).sendMessage(
				new SendMessageRequest(QUEUE_URL, this.objectMapper.writeValueAsString(EVENT_TASK)));
		this.verify();
	}

	@Test(groups = "automated")
	public void testEnqueueFails() throws Exception {

		final SendMessageRequest sendMessageRequest = new SendMessageRequest(QUEUE_URL,
				this.objectMapper.writeValueAsString(EVENT_TASK));
		Mockito.when(this.mockClient.sendMessage(sendMessageRequest)).thenThrow(new AmazonClientException(""));

		try {
			this.queueService.enqueue(EVENT_TASK);
			Assert.fail("Expected EnqueueFailedException");
		} catch (final EnqueueFailedException e) {

		}

		Mockito.verify(this.mockClient).sendMessage(sendMessageRequest);
		this.verify();
	}

	@Test(groups = "automated")
	public void testEnqueueBatch() throws Exception {
		this.queueService.enqueueBatch(Arrays.asList(EVENT_TASK));

		final List<SendMessageBatchRequestEntry> entries = Lists.newArrayList(new SendMessageBatchRequestEntry("0",
				this.objectMapper.writeValueAsString(EVENT_TASK)));

		Mockito.verify(this.mockClient).sendMessageBatch(new SendMessageBatchRequest(QUEUE_URL, entries));
		this.verify();
	}

	@Test(groups = "automated")
	public void testEnqueueBatchMultiple() throws Exception {
		this.queueService.enqueueBatch(Arrays.asList(EVENT_TASK, EVENT_TASK));

		final List<SendMessageBatchRequestEntry> entries = Lists.newArrayList(new SendMessageBatchRequestEntry("0",
				this.objectMapper.writeValueAsString(EVENT_TASK)), new SendMessageBatchRequestEntry("1",
				this.objectMapper.writeValueAsString(EVENT_TASK)));

		Mockito.verify(this.mockClient).sendMessageBatch(new SendMessageBatchRequest(QUEUE_URL, entries));
		this.verify();
	}

	@Test(groups = "automated")
	public void testEnqueueBatchFails() throws Exception {

		final List<SendMessageBatchRequestEntry> entries = Lists.newArrayList(new SendMessageBatchRequestEntry("0",
				this.objectMapper.writeValueAsString(EVENT_TASK)), new SendMessageBatchRequestEntry("1",
				this.objectMapper.writeValueAsString(EVENT_TASK)));

		Mockito.when(this.mockClient.sendMessageBatch(new SendMessageBatchRequest(QUEUE_URL, entries))).thenThrow(
				new AmazonClientException(null));

		try {
			this.queueService.enqueueBatch(Arrays.asList(EVENT_TASK, EVENT_TASK));
			Assert.fail("Expected EnqueueFailedException");
		} catch (final EnqueueFailedException e) {

		}

		Mockito.verify(this.mockClient).sendMessageBatch(new SendMessageBatchRequest(QUEUE_URL, entries));
		this.verify();
	}

	@Test(groups = "automated")
	public void testLease() throws Exception {

		final String eventTaskJson = this.objectMapper.writeValueAsString(EVENT_TASK);
		final ReceiveMessageRequest request = new ReceiveMessageRequest(QUEUE_URL).withMaxNumberOfMessages(10);

		Mockito.when(this.mockClient.receiveMessage(request)).thenReturn(
				new ReceiveMessageResult()
						.withMessages(new Message().withReceiptHandle(HANDLE).withBody(eventTaskJson)));

		final List<Task> lease = this.queueService.lease();

		Assert.assertEquals(lease, Lists.newArrayList(new Task(HANDLE, eventTaskJson)));

		Mockito.verify(this.mockClient).receiveMessage(request);
		this.verify();

	}

	@Test(groups = "automated")
	public void testLeaseNothing() throws Exception {

		final ReceiveMessageRequest request = new ReceiveMessageRequest(QUEUE_URL).withMaxNumberOfMessages(10);

		Mockito.when(this.mockClient.receiveMessage(request)).thenReturn(new ReceiveMessageResult());

		final List<Task> lease = this.queueService.lease();

		Assert.assertTrue(lease.isEmpty());

		Mockito.verify(this.mockClient).receiveMessage(request);
		this.verify();
	}

	@Test(groups = "automated")
	public void testDelete() throws Exception {
		this.queueService.delete(Arrays.asList(TASK));

		final List<DeleteMessageBatchRequestEntry> entries = Lists.newArrayList(new DeleteMessageBatchRequestEntry("0",
				HANDLE));

		Mockito.verify(this.mockClient).deleteMessageBatch(new DeleteMessageBatchRequest(QUEUE_URL, entries));
	}

	@Test(groups = "automated")
	public void testDeleteMultiple() throws Exception {
		this.queueService.delete(Arrays.asList(TASK, TASK));

		final List<DeleteMessageBatchRequestEntry> entries = Lists.newArrayList(new DeleteMessageBatchRequestEntry("0",
				HANDLE), new DeleteMessageBatchRequestEntry("1", HANDLE));

		Mockito.verify(this.mockClient).deleteMessageBatch(new DeleteMessageBatchRequest(QUEUE_URL, entries));
	}

	@Test(groups = "automated")
	public void testSerializationFails() throws Exception {
		Mockito.when(this.mockObjectMapper.writeValueAsString(Matchers.anyString())).thenThrow(new IOException());
		final AmazonSimpleQueueService<EventTask> service = new AmazonSimpleQueueService<>(this.mockClient,
				this.mockObjectMapper, QUEUE_URL);

		try {
			service.enqueue(EVENT_TASK);
			Assert.fail("Expected EnqueueFailedException");
		} catch (final EnqueueFailedException e) {

		}

		Mockito.verify(this.mockObjectMapper).writeValueAsString(EVENT_TASK);
		this.verify();
	}
}
