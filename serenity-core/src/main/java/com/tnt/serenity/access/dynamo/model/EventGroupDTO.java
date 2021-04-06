package com.tnt.serenity.access.dynamo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Wither
@DynamoDBTable(tableName = "EventGroup")
public class EventGroupDTO {

	private String id;

	private String gameId;

	private String name;

	@DynamoDBHashKey
	public String getId() {
		return this.id;
	}

	@DynamoDBIndexHashKey(globalSecondaryIndexName = "gameId")
	public String getGameId() {
		return this.gameId;
	}

}
