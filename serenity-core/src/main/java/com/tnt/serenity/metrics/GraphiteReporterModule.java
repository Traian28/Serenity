package com.tnt.serenity.metrics;

import com.google.inject.AbstractModule;

public class GraphiteReporterModule extends AbstractModule {

	@Override
	protected void configure() {
		this.bind(HostedGraphiteReporterConfig.class).asEagerSingleton();
	}

}
