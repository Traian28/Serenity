package com.tnt.serenity;

import java.util.concurrent.ExecutorService;

import com.tnt.serenity.metrics.GraphiteReporterModule;
import com.tnt.serenity.processor.TaskProcessor;
import com.tnt.serenity.processor.impl.CsvTaskProcessor;
import com.tnt.serenity.processor.impl.JsonTaskProcessor;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import com.amazonaws.services.s3.AmazonS3Client;
import com.ea.tnt.config.AppConfigModule;
import com.tnt.serenity.output.FileOutputer;
import com.tnt.serenity.output.TimedS3RolloverFileOutputer;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import com.yammer.metrics.guice.InstrumentationModule;

public class App {

	public static void main(final String[] args) {
		final Injector injector = Guice.createInjector(new SerenityModule(), new AppConfigModule(), new GraphiteReporterModule(), new InstrumentationModule(),
				new AbstractModule() {

					@Override
					protected void configure() {
						this.bind(AmazonS3Client.class).toProvider(AmazonS3ClientProvider.class).asEagerSingleton();
						this.bind(CsvMapper.class).toInstance(new CsvMapper());
						
						final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
						mapperFactory.getConverterFactory().registerConverter(new PassThroughConverter(org.joda.time.DateTime.class));
						this.bind(MapperFacade.class).toInstance(mapperFactory.getMapperFacade());

						final Multibinder<TaskProcessor> binder = Multibinder.<TaskProcessor> newSetBinder(this.binder(), TaskProcessor.class);
						binder.addBinding().to(JsonTaskProcessor.class).asEagerSingleton();
						binder.addBinding().to(CsvTaskProcessor.class).asEagerSingleton();

						this.bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class).asEagerSingleton();
						this.bind(FileOutputer.class).to(TimedS3RolloverFileOutputer.class).asEagerSingleton();
						this.bind(SerenityBackend.class).asEagerSingleton();
					}
				});

		injector.getInstance(SerenityBackend.class).run();
	}
}
