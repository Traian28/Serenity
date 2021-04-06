package com.tnt.serenity.access.dynamo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.tnt.serenity.access.dynamo.model.EventGroupDTO;
import com.tnt.serenity.access.dynamo.model.CustomEventSchemaDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class TableCreatorScript {
	
	private static final Long TABLE_READ_THROUGHPUT = 10L;
	
	private static final Long TABLE_WRITE_THROUGHPUT = 10L;
	
	private static final Long INDEX_READ_THROUGHPUT = 10L;
	
	private static final Long INDEX_WRITE_THROUGHPUT = 10L;

	public static void main(final String[] args) {

		String endpoint = "http://localhost:8091/";
		String tablePrefix = "auth.local";

		if (args.length == 1) {
			endpoint = args[0];
		}
		
		if(args.length == 2) {
			endpoint = args[0];
			tablePrefix = args[1];
		}

		createTable(EventGroupDTO.class, tablePrefix, endpoint);
		createTable(CustomEventSchemaDTO.class, tablePrefix, endpoint);
		
		System.out.println("\n\n\nDone.");
	}

	public static <T> void createTable(final Class<T> clazz, final String tablePrefix) {
		createTable(clazz, tablePrefix, null);
	}

	public static <T> void createTable(final Class<T> clazz, final String tablePrefix, final String endpoint) {
		
		System.out.println("\n\n");
		System.out.println(String.format("Creating table [%s.%s]", tablePrefix, clazz.getName()));
		System.out.println("-------------------------------------------------------------------------------------");
		
		final AWSCredentials credentials = new BasicAWSCredentials("dummyKeyAccessC", "dummyKeySecretC");
		final AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);

		if (StringUtils.isNotBlank(endpoint)) {
			client.setEndpoint(endpoint);
		}

		final String tableName = getTableName(clazz, tablePrefix);

		boolean exists = true;
		try {
			client.describeTable(tableName);
		} catch (final Exception ex) {
			exists = false;
		}
		if (exists) {
			client.deleteTable(tableName);
			waitForTableToBeDeleted(client, tableName);
		}

		final CreateTableRequest request = new CreateTableRequest().withTableName(tableName);
		
		KeySchemaElement hashKey = null;
		KeySchemaElement rangeKey = null;
		
		final Map<String, KeySchemaElement> globalIndexHashKeys = Maps.newHashMap();
		final Map<String, KeySchemaElement> globalIndexRangeKeys = Maps.newHashMap();
		
		final Map<String, KeySchemaElement> localIndexRangeKeys = Maps.newHashMap();
		
		final List<String> keys = Lists.newArrayList();
		
		for (final Method m : clazz.getMethods()) {
			
			for (final Annotation a : m.getAnnotations()) {
				
				boolean key = false;
				String attributeName = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
				
				if (a.annotationType() == (DynamoDBHashKey.class)) {
					
					final DynamoDBHashKey annotation = (DynamoDBHashKey) a;
					
					if (StringUtils.isNotBlank(annotation.attributeName())) {
						attributeName = annotation.attributeName();
					}
					
					hashKey = new KeySchemaElement().withAttributeName(attributeName).withKeyType(KeyType.HASH);
					
					key = true;
				
				} else if (a.annotationType() == (DynamoDBRangeKey.class)) {
					
					final DynamoDBRangeKey annotation = (DynamoDBRangeKey) a;
					
					if (StringUtils.isNotBlank(annotation.attributeName())) {
						attributeName = annotation.attributeName();
					}
					
					rangeKey = new KeySchemaElement().withAttributeName(attributeName).withKeyType(KeyType.RANGE);
					
					key = true;
				
				} else if (a.annotationType() == (DynamoDBIndexHashKey.class)) {
					
					final DynamoDBIndexHashKey annotation = (DynamoDBIndexHashKey) a;
					
					if (annotation.globalSecondaryIndexNames() != null && annotation.globalSecondaryIndexNames().length > 0) {
						throw new NotImplementedException("Setting multiple global secondary index names is not yet supported in TableCreatorScript");
					}
					
					if (!StringUtils.isBlank(annotation.globalSecondaryIndexName())) {
						
						final String indexName = annotation.globalSecondaryIndexName();
						
						if (StringUtils.isNotBlank(annotation.attributeName())) {
							attributeName = annotation.attributeName();
						}
						
						globalIndexHashKeys.put(indexName, new KeySchemaElement().withAttributeName(attributeName).withKeyType(KeyType.HASH));
						
					} else {
						throw new IllegalArgumentException("globalSecondaryIndexName required on @DynamoDBIndexHashKey fields");
					}
					
					key = true;
					
				} else if (a.annotationType() == (DynamoDBIndexRangeKey.class)) {
					
					final DynamoDBIndexRangeKey annotation = (DynamoDBIndexRangeKey) a;
					
					if (annotation.globalSecondaryIndexNames() != null && annotation.globalSecondaryIndexNames().length > 0) {
						throw new NotImplementedException("Setting multiple global secondary index names is not yet supported in TableCreatorScript");
					}
					
					if (annotation.localSecondaryIndexNames() != null && annotation.localSecondaryIndexNames().length > 0) {
						throw new NotImplementedException("Setting multiple local secondary index names is not yet supported in TableCreatorScript");
					}
					
					if (!StringUtils.isBlank(annotation.globalSecondaryIndexName())) {
						
						final String indexName = annotation.globalSecondaryIndexName();
						
						if (StringUtils.isNotBlank(annotation.attributeName())) {
							attributeName = annotation.attributeName();
						}
						
						globalIndexRangeKeys.put(indexName, new KeySchemaElement().withAttributeName(attributeName).withKeyType(KeyType.RANGE));
						
					} else {
						
						if (StringUtils.isNotBlank(annotation.attributeName())) {
							attributeName = annotation.attributeName();
						}
						
						final String indexName = StringUtils.isBlank(annotation.localSecondaryIndexName()) ? String.format("%s-index", attributeName) : annotation.localSecondaryIndexName();
						
						localIndexRangeKeys.put(indexName, new KeySchemaElement().withAttributeName(attributeName).withKeyType(KeyType.RANGE));
					}
					
					key = true;
				}
				
				if (key) {
					
					if(!keys.contains(attributeName)) {
						
						ScalarAttributeType attributeType = ScalarAttributeType.S;
						
						if (m.getReturnType().equals(boolean.class)) {
							attributeType = ScalarAttributeType.N;
						}
						
						if (m.getReturnType().equals(Integer.class)) {
							attributeType = ScalarAttributeType.N;
						}
						
						if (m.getReturnType().equals(Double.class)) {
							attributeType = ScalarAttributeType.N;
						}
						
						if (m.getReturnType().equals(Float.class)) {
							attributeType = ScalarAttributeType.N;
						}

						request.withAttributeDefinitions(new AttributeDefinition().withAttributeName(attributeName).withAttributeType(attributeType));
						
					}
					
					keys.add(attributeName);
					
				}

			}
		}
		
		if(rangeKey != null) {
			request.withKeySchema(hashKey, rangeKey);
		} else {
			request.withKeySchema(hashKey);
		}
		
		if(!localIndexRangeKeys.isEmpty()) {
			
			for (final String indexName : localIndexRangeKeys.keySet()) {
				request.withLocalSecondaryIndexes(new LocalSecondaryIndex().withIndexName(indexName)
						.withProjection(new Projection().withProjectionType(ProjectionType.ALL))
						.withKeySchema(hashKey, localIndexRangeKeys.get(indexName)));
			}
			
			
		}
		
		if (!globalIndexHashKeys.isEmpty()) {

			for (final String indexName : globalIndexHashKeys.keySet()) {
				
				final KeySchemaElement hashKeySchema = globalIndexHashKeys.get(indexName);
				final KeySchemaElement rangeKeySchema = globalIndexRangeKeys.get(indexName);
				
				request.withGlobalSecondaryIndexes(new GlobalSecondaryIndex().withIndexName(indexName)
						.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(INDEX_READ_THROUGHPUT).withWriteCapacityUnits(INDEX_WRITE_THROUGHPUT))
						.withProjection(new Projection().withProjectionType(ProjectionType.ALL)).withKeySchema(hashKeySchema, rangeKeySchema));
			}

		}

		request.setProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(TABLE_READ_THROUGHPUT).withWriteCapacityUnits(TABLE_WRITE_THROUGHPUT));

		
		final CreateTableResult result = client.createTable(request);
		
		System.out.println();
		System.out.println("Table created with schema:");
		System.out.println(result.getTableDescription());
	}

	public static <T> String getTableName(final Class<T> clazz, final String tablePrefix) {
		String tableName = clazz.getSimpleName();
		for (final Annotation a : clazz.getAnnotations()) {
			if (a.annotationType().equals(DynamoDBTable.class)) {
				final DynamoDBTable db = (DynamoDBTable) a;
				tableName = tablePrefix + "." + db.tableName();
			}
		}
		return tableName;
	}

	public static void waitForTableToBecomeAvailable(final AmazonDynamoDBClient client, final String tableName) {
		System.out.println("Waiting for " + tableName + " to become ACTIVE...");

		final long startTime = System.currentTimeMillis();
		final long endTime = startTime + (10 * 60 * 1000);
		while (System.currentTimeMillis() < endTime) {
			final DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
			final TableDescription tableDescription = client.describeTable(request).getTable();
			final String tableStatus = tableDescription.getTableStatus();
			System.out.println("  - current state: " + tableStatus);
			if (tableStatus.equals(TableStatus.ACTIVE.toString())) {
				return;
			}
			try {
				Thread.sleep(1000 * 20);
			} catch (final Exception e) {
			}
		}
		throw new RuntimeException("Table " + tableName + " never went active");
	}

	public static void deleteTable(final AmazonDynamoDBClient client, final String tableName) {
		try {

			final DeleteTableRequest request = new DeleteTableRequest().withTableName(tableName);

			client.deleteTable(request);

		} catch (final AmazonServiceException ase) {
			System.err.println("Failed to delete table " + tableName + " " + ase);
		}

	}

	public static void waitForTableToBeDeleted(final AmazonDynamoDBClient client, final String tableName) {
		System.out.println("Waiting for " + tableName + " while status DELETING...");

		final long startTime = System.currentTimeMillis();
		final long endTime = startTime + (10 * 60 * 1000);
		while (System.currentTimeMillis() < endTime) {
			try {
				final DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
				final TableDescription tableDescription = client.describeTable(request).getTable();
				final String tableStatus = tableDescription.getTableStatus();
				System.out.println("  - current state: " + tableStatus);
				if (tableStatus.equals(TableStatus.ACTIVE.toString())) {
					return;
				}
			} catch (final ResourceNotFoundException e) {
				System.out.println("Table " + tableName + " is not found. It was deleted.");
				return;
			}
			try {
				Thread.sleep(1000 * 20);
			} catch (final Exception e) {
			}
		}
		throw new RuntimeException("Table " + tableName + " was never deleted");
	}
}