package com.tnt.serenity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Wither
public class EventGroup {

	private String id;
	
	private String gameId;

	private String name;

}
