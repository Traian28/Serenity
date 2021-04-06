package com.tnt.serenity.access.dynamo.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Wither
@DynamoDBTable(tableName = "CustomEventSchema")
public class CustomEventSchemaDTO {

	private String id;

	private String eventName;

	private String gameId;

	private String jsonSchema;

	@DynamoDBHashKey
	public String getId() {
		return this.id;
	}

	@DynamoDBIndexHashKey(globalSecondaryIndexName = "gameId-eventName")
	public String getGameId() {
		return this.gameId;
	}

	@DynamoDBIndexRangeKey(globalSecondaryIndexName = "gameId-eventName")
	public String getEventName() {
		return this.eventName;
	}

}
