package com.tnt.serenity;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.tnt.serenity.access.CustomEventSchemaAccess;
import com.tnt.serenity.access.EventGroupAccess;
import com.tnt.serenity.access.dynamo.AmazonDynamoDBClientProvider;
import com.tnt.serenity.access.dynamo.DynamoCustomEventSchemaAccess;
import com.tnt.serenity.access.dynamo.DynamoDBMapperProvider;
import com.tnt.serenity.access.dynamo.DynamoEventGroupAccess;
import com.tnt.serenity.model.EventTask;
import com.tnt.serenity.service.QueueService;
import com.tnt.serenity.service.amazon.AmazonSQSAsyncProvider;
import com.tnt.serenity.service.amazon.AmazonSimpleQueueService;
import com.tnt.serenity.validation.CustomEventValidator;
import com.tnt.serenity.validation.EventDefaults;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import ma.glasnost.orika.MapperFacade;
import org.codehaus.jackson.map.ObjectMapper;

public class SerenityModule extends AbstractModule {

	@Override
	protected void configure() {

		this.bind(ObjectMapperProvider.class).asEagerSingleton();
		this.bind(MapperFacade.class).toProvider(MapperFacadeProvider.class).asEagerSingleton();
		
		this.bind(EventDefaults.class).asEagerSingleton();

		this.bind(JsonSchemaFactory.class).toInstance(JsonSchemaFactory.byDefault());
		this.bind(CustomEventValidator.class).asEagerSingleton();

		this.bind(ObjectMapper.class).toInstance(new ObjectMapperProvider().getContext(null));
		
		this.bind(AmazonSQSAsync.class).toProvider(AmazonSQSAsyncProvider.class).asEagerSingleton();
		this.bind(QueueService.class).to(AmazonSimpleQueueService.class).asEagerSingleton();
		this.bind(new TypeLiteral<QueueService<EventTask>>() {
		}).to(new TypeLiteral<AmazonSimpleQueueService<EventTask>>() {
		}).asEagerSingleton();

		this.bind(AmazonDynamoDBClient.class).toProvider(AmazonDynamoDBClientProvider.class).asEagerSingleton();
		this.bind(DynamoDBMapper.class).toProvider(DynamoDBMapperProvider.class).asEagerSingleton();

		this.bind(EventGroupAccess.class).to(DynamoEventGroupAccess.class).asEagerSingleton();
		this.bind(CustomEventSchemaAccess.class).to(DynamoCustomEventSchemaAccess.class).asEagerSingleton();
		
	}

}
