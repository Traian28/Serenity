package com.tnt.serenity.model;


import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

@AllArgsConstructor
@JsonPropertyOrder({ "name, userId, platform, sessionId, type, timestamp, serverTimestamp, tntId, adType, adProvider, adSegmentation, adPublisherId, adAppId, adZoneId, adCurrencyId, adCurrencyAmount, adNotRequestedReason" })
public class CsvAdEvent {

	private final Event event;
	
	
	@JsonIgnore
	public static CsvSchema getCsvSchema(){
		
		// sadly, have to do this because for some reason CsvMapper doesn't respect the JsonPropertyOrder up there
		CsvSchema schema = CsvSchema.builder()
		        .addColumn("name")
		        .addColumn("userId")
		        .addColumn("platform")
		        .addColumn("sessionId")
		        .addColumn("type")
		        .addColumn("timestamp")
		        .addColumn("serverTimestamp")
		        .addColumn("tntId")
		        .addColumn("adType")
		        .addColumn("adProvider")
		        .addColumn("adSegmentation")
		        .addColumn("adPublisherId")
		        .addColumn("adAppId")
		        .addColumn("adZoneId")
		        .addColumn("adCurrencyId")
		        .addColumn("adCurrencyAmount")
		        .addColumn("adNotRequestedReason")
		        .build();
		
		return schema;
	}

	public String getName() {
		return this.event.getName();
	}

	public String getUserId() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.USER_ID_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.USER_ID_KEY).toString();
		}
		return result;
	}

	public String getPlatform() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.PLATFORM_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.PLATFORM_KEY).toString();
		}
		return result;
	}

	public String getSessionId() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.SESSION_ID_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.SESSION_ID_KEY).toString();
		}
		return result;
	}

	public String getType() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.TYPE_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.TYPE_KEY).toString();
		}
		return result;
	}

	public String getTimestamp() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.TIMESTAMP_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.TIMESTAMP_KEY).toString();
		}
		return result;
	}

	public String getServerTimestamp() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.SERVER_TIMESTAMP_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.SERVER_TIMESTAMP_KEY).toString();
		}
		return result;
	}

	public String getTntId() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.TNT_ID_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.TNT_ID_KEY).toString();
		}
		return result;
	}

	public String getAdType() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.AD_TYPE_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.AD_TYPE_KEY).toString();
		}
		return result;
	}

	public String getAdProvider() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.AD_PROVIDER_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.AD_PROVIDER_KEY).toString();
		}
		return result;
	}

	public String getAdSegmentation() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.AD_SEGMENTATION_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.AD_SEGMENTATION_KEY).toString();
		}
		return result;
	}

	public String getAdPublisherId() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.AD_PUBLISHER_ID_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.AD_PUBLISHER_ID_KEY).toString();
		}
		return result;
	}

	public String getAdAppId() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.AD_APP_ID_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.AD_APP_ID_KEY).toString();
		}
		return result;
	}

	public String getAdZoneId() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.AD_ZONE_ID_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.AD_ZONE_ID_KEY).toString();
		}
		return result;
	}

	public String getAdCurrencyId() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.AD_CURRENCY_ID_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.AD_CURRENCY_ID_KEY).toString();
		}
		return result;
	}

	public String getAdCurrencyAmount() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.AD_CURRENCY_AMOUNT_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.AD_CURRENCY_AMOUNT_KEY).toString();
		}
		return result;
	}

	public String getAdNotRequestedReason() {
		String result = null;
		if (this.event.getParameters().containsKey(EventParameterKeys.AD_NOT_REQUESTED_REASON_KEY)) {
			result = this.event.getParameters().get(EventParameterKeys.AD_NOT_REQUESTED_REASON_KEY).toString();
		}
		return result;
	}
}
