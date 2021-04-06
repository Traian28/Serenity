package com.tnt.serenity;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.tnt.serenity.service.CustomEventSchemaService;
import com.tnt.serenity.model.Event;
import com.tnt.serenity.model.EventTask;
import com.tnt.serenity.service.QueueService;
import com.tnt.serenity.validation.EventDefaults;
import com.ea.tnt.service.validation.Validator;
import com.google.inject.*;
import com.google.inject.name.Names;
import ma.glasnost.orika.MapperFacade;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class SerenityModuleTest {

	@Test(groups = "automated")
	public void testObjectMapperProvider() {
		final ObjectMapperProvider instance = this.getInjector().getInstance(ObjectMapperProvider.class);
		Assert.assertNotNull(instance);
	}

	@Test(groups = "automated")
	public void testEventDefaults() {
		final EventDefaults instance = this.getInjector().getInstance(EventDefaults.class);
		Assert.assertNotNull(instance);

	}

	@Test(groups = "automated")
	public void testEventValidator() {
		final Validator<Event> instance = this.getInjector().getInstance(Key.get(new TypeLiteral<Validator<Event>>() {
		}));
		Assert.assertNotNull(instance);

	}

	@Test(groups = "automated")
	public void testObjectMapper() {
		final ObjectMapper instance = this.getInjector().getInstance(ObjectMapper.class);
		Assert.assertNotNull(instance);

	}

	@Test(groups = "automated")
	public void testAmazonSQSAsync() {
		final AmazonSQSAsync instance = this.getInjector().getInstance(AmazonSQSAsync.class);
		Assert.assertNotNull(instance);

	}

	@Test(groups = "automated")
	public void testQueueService() {
		final QueueService<?> instance = this.getInjector().getInstance(QueueService.class);
		Assert.assertNotNull(instance);

	}
	
	@Test(groups = "automated")
	public void testMapperFacade() {
		final MapperFacade instance = this.getInjector().getInstance(MapperFacade.class);
		Assert.assertNotNull(instance);
	}
	
	@Test(groups = "automated")
	public void testCustomEventSchemaService() {
		final CustomEventSchemaService instance = this.getInjector().getInstance(CustomEventSchemaService.class);
		Assert.assertNotNull(instance);

	}

	@Test(groups = "automated")
	public void testEventTaskQueueService() {
		final QueueService<EventTask> instance = this.getInjector().getInstance(
				Key.get(new TypeLiteral<QueueService<EventTask>>() {
				}));
		Assert.assertNotNull(instance);

	}

	private Injector getInjector() {
		return Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				this.bindConstant().annotatedWith(Names.named("aws.sqs.access.key")).to(UUID.randomUUID().toString());
				this.bindConstant().annotatedWith(Names.named("aws.sqs.secret.key")).to(UUID.randomUUID().toString());
				this.bindConstant().annotatedWith(Names.named("aws.sqs.queue.url")).to(UUID.randomUUID().toString());
				this.bindConstant().annotatedWith(Names.named("aws.dynamo.table.name.prefix")).to(UUID.randomUUID().toString());
				this.bindConstant().annotatedWith(Names.named("aws.dynamo.endpoint")).to(UUID.randomUUID().toString());
				this.bindConstant().annotatedWith(Names.named("aws.dynamo.access.key")).to(UUID.randomUUID().toString());
				this.bindConstant().annotatedWith(Names.named("aws.dynamo.secret.key")).to(UUID.randomUUID().toString());
				
				//final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
				//mapperFactory.getConverterFactory().registerConverter(new PassThroughConverter(org.joda.time.DateTime.class));
				//this.bind(MapperFacade.class).toInstance(mapperFactory.getMapperFacade());
			}
		}, new SerenityModule());
	}

}
