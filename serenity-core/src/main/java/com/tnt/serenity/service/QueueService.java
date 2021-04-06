package com.tnt.serenity.service;

import java.util.List;

import com.tnt.serenity.exception.EnqueueFailedException;

public interface QueueService<T> {

	void enqueue(final T obj) throws EnqueueFailedException;

	void enqueueBatch(final List<T> list) throws EnqueueFailedException;

	List<Task> lease();

	void delete(final List<Task> tasks);

}
