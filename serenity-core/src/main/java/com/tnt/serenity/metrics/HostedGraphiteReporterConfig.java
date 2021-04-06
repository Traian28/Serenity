package com.tnt.serenity.metrics;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.logback.InstrumentedAppender;
import com.yammer.metrics.reporting.GraphiteReporter;

@Slf4j
public class HostedGraphiteReporterConfig {

	private final String graphiteApiKey;

	private final String graphiteHost;

	private final int graphitePort;

	private final String serviceId;

	private final String environmentName;

	private final String serviceVersion;

	private final MetricsRegistry registry;

	@Inject
	public HostedGraphiteReporterConfig(@Named("hosted.graphite.key") final String graphiteApiKey,
			@Named("hosted.graphite.host") final String graphiteHost,
			@Named("hosted.graphite.port") final int graphitePort, @Named("service.id") final String serviceId,
			@Named("environment.name") final String environmentName,
			@Named("service.version") final String serviceVersion, final MetricsRegistry registry) {
		super();
		this.graphiteApiKey = graphiteApiKey;
		this.graphiteHost = graphiteHost;
		this.graphitePort = graphitePort;
		this.serviceId = serviceId;
		this.environmentName = environmentName;
		this.serviceVersion = serviceVersion;
		this.registry = registry;

		this.configure();
		this.configureMeteredLogging();
	}

	private void configure() {

		final String prefix = String.format("%s.tnt-serenity-%s.%s.%s", this.graphiteApiKey, this.environmentName,
				this.serviceId, this.serviceVersion);
		log.info("Sending metrics to graphite using prefix [{}]", prefix);

		GraphiteReporter.enable(this.registry, 1, TimeUnit.MINUTES, this.graphiteHost, this.graphitePort, prefix);
	}

	private void configureMeteredLogging() {
		final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
		final Logger root = factory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

		final InstrumentedAppender metrics = new InstrumentedAppender(this.registry);
		metrics.setContext(root.getLoggerContext());
		metrics.start();
		root.addAppender(metrics);
	}

}
