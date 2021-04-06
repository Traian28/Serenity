package com.tnt.serenity.service.amazon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.codehaus.jackson.map.ObjectMapper;

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
import com.tnt.serenity.exception.EnqueueFailedException;
import com.tnt.serenity.service.QueueService;
import com.tnt.serenity.service.Task;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Timed;

@Slf4j
public class AmazonSimpleQueueService<T> implements QueueService<T> {

	private final AmazonSQSAsync client;

	private final String queueUrl;

	private final ObjectMapper objectMapper;

	@Inject
	public AmazonSimpleQueueService(final AmazonSQSAsync client, final ObjectMapper objectMapper,
			@Named("aws.sqs.queue.url") final String queueUrl) {
		super();
		this.objectMapper = objectMapper;
		this.client = client;
		this.queueUrl = queueUrl;
	}

	@Override
	@Timed(rateUnit = TimeUnit.SECONDS, durationUnit = TimeUnit.MILLISECONDS)
	@ExceptionMetered(rateUnit = TimeUnit.SECONDS)
	public void enqueue(final T obj) throws EnqueueFailedException {

		final String json = this.serialize(obj);
		log.debug("Enqueuing {} to queue {}", json, this.queueUrl);

		try {
			this.client.sendMessage(new SendMessageRequest(this.queueUrl, json));
		} catch (final AmazonClientException e) {
			log.error("Unable to enqueue object [{}]", json, e);
			throw new EnqueueFailedException("Unable to serialize object to enqueue", e);
		}
	}

	@Override
	@Timed(rateUnit = TimeUnit.SECONDS, durationUnit = TimeUnit.MILLISECONDS)
	@ExceptionMetered(rateUnit = TimeUnit.SECONDS)
	public void enqueueBatch(final List<T> list) throws EnqueueFailedException {

		final List<SendMessageBatchRequestEntry> entries = new ArrayList<SendMessageBatchRequestEntry>();
		int i = 0;

		for (final T item : list) {
			final String json = this.serialize(item);
			entries.add(new SendMessageBatchRequestEntry(String.format("%s", i), json));
			log.debug("Enqueuing {} to queue {}", json, this.queueUrl);
			i++;
		}

		try {
			this.client.sendMessageBatch(new SendMessageBatchRequest(this.queueUrl, entries));
		} catch (final AmazonClientException e) {
			log.error("Enqueueing tasks failed", e);
			throw new EnqueueFailedException("Enqueueing tasks failed", e);
		}
	}

	@Override
	@Timed(rateUnit = TimeUnit.SECONDS, durationUnit = TimeUnit.MILLISECONDS)
	@ExceptionMetered(rateUnit = TimeUnit.SECONDS)
	public List<Task> lease() {

		final ReceiveMessageResult receiveMessage = this.client.receiveMessage(new ReceiveMessageRequest(this.queueUrl)
				.withMaxNumberOfMessages(10));

		final List<Task> list = new ArrayList<Task>();

		for (final Message message : receiveMessage.getMessages()) {
			list.add(new Task(message.getReceiptHandle(), message.getBody()));
		}

		return list;
	}

	@Override
	@Timed(rateUnit = TimeUnit.SECONDS, durationUnit = TimeUnit.MILLISECONDS)
	@ExceptionMetered(rateUnit = TimeUnit.SECONDS)
	public void delete(final List<Task> tasks) {

		final List<DeleteMessageBatchRequestEntry> entries = new ArrayList<DeleteMessageBatchRequestEntry>();
		int i = 0;

		for (final Task task : tasks) {
			entries.add(new DeleteMessageBatchRequestEntry(String.format("%s", i), task.getHandle()));
			i++;
		}

		this.client.deleteMessageBatch(new DeleteMessageBatchRequest(this.queueUrl, entries));
	}

	private String serialize(final T obj) throws EnqueueFailedException {
		try {
			return this.objectMapper.writeValueAsString(obj);
		} catch (final IOException e) {
			log.error("Unable to serialize object to enqueue", e);
			throw new EnqueueFailedException("Unable to serialize object to enqueue", e);
		}
	}
}
