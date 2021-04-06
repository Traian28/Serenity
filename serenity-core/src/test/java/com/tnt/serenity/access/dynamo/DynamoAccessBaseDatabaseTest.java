package com.tnt.serenity.access.dynamo;

import java.security.PrivateKey;
import java.security.PublicKey;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DynamoAccessBaseDatabaseTest<T> {

	private static final String TABLE_PREFIX = "serenity.database-integration-test";

	private final Class<T> modelClass;

	private final String tableName;

	protected final MapperFactory mapperFactory;

	protected final DynamoDBMapper dynamoMapper;

	protected final AmazonDynamoDBClient amazonDynamoDBClient;

	public DynamoAccessBaseDatabaseTest(final Class<T> modelClass) {
		super();
		this.modelClass = modelClass;
		this.mapperFactory = new DefaultMapperFactory.Builder().build();
		this.mapperFactory.getConverterFactory().registerConverter(new PassThroughConverter(org.joda.time.DateTime.class, PublicKey.class, PrivateKey.class));
		this.amazonDynamoDBClient = new AmazonDynamoDBClientProvider("dummyKeyAccessC", "dummyKeySecretC", null).get();
		this.dynamoMapper = new DynamoDBMapperProvider(this.amazonDynamoDBClient, TABLE_PREFIX + ".").get();
		this.tableName = TableCreatorScript.getTableName(this.modelClass, TABLE_PREFIX);
	}

	@BeforeClass(groups = "database")
	public void setupClass() {
		TableCreatorScript.createTable(this.modelClass, TABLE_PREFIX);
		TableCreatorScript.waitForTableToBecomeAvailable(this.amazonDynamoDBClient, this.tableName);
	}

	@AfterClass(groups = "database")
	public void afterClass() {
		TableCreatorScript.deleteTable(this.amazonDynamoDBClient, this.tableName);
		TableCreatorScript.waitForTableToBeDeleted(this.amazonDynamoDBClient, this.tableName);
	}

}
