package com.tnt.serenity.output;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.inject.Inject;
import com.google.inject.name.Named;

@Slf4j
public class TimedS3RolloverFileOutputer implements FileOutputer {

	private final ConcurrentHashMap<String, File> files;

	private final ScheduledExecutorService executorService;

	private final AmazonS3Client client;

	private final String bucket;

	private final Integer interval;

	private final String newLine;

	@Inject
	public TimedS3RolloverFileOutputer(final AmazonS3Client client, @Named("aws.s3.bucket") final String bucket,
			@Named("aws.s3.upload.interval.seconds") final Integer interval) {
		this.client = client;
		this.bucket = bucket;
		this.interval = interval;
		this.files = new ConcurrentHashMap<>();
		this.executorService = Executors.newScheduledThreadPool(20);
		this.executorService.scheduleWithFixedDelay(new RolloverRunnable(), this.interval, this.interval,
				TimeUnit.SECONDS);
		this.newLine = System.lineSeparator();
	}

	@Override
	public void write(final String key, final String data) {
		final File file = this.getOrCreateFile(key);
		try {
			FileUtils.writeStringToFile(file, String.format("%s%s", data, this.newLine), true);
		} catch (final IOException e) {
			log.error("Failed to write data to file with key [{}]", key, e);
		}
	}

	private File newFile(final String key) {
		return new File(String.format("data/%s/%s.log", key, UUID.randomUUID().toString()));
	}

	private File getOrCreateFile(final String key) {
		this.files.putIfAbsent(key, this.newFile(key));
		return this.files.get(key);
	}

	private class RolloverRunnable implements Runnable {

		@Override
		public void run() {

			for (final String key : TimedS3RolloverFileOutputer.this.files.keySet()) {

				final File oldFile = TimedS3RolloverFileOutputer.this.getOrCreateFile(key);

				if (oldFile.exists()) {

					log.debug("Rolling over file for key [{}]", oldFile);

					TimedS3RolloverFileOutputer.this.files.remove(key, oldFile);

					final String oldFilePath = oldFile.toString();
					final String s3Key = oldFilePath.substring(5, oldFilePath.length());

					final PutObjectRequest request = new PutObjectRequest(TimedS3RolloverFileOutputer.this.bucket,
							s3Key, oldFile);
					TimedS3RolloverFileOutputer.this.client.putObject(request);

					FileUtils.deleteQuietly(oldFile);
				}
			}
		}

	}

}
