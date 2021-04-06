package com.tnt.serenity.access;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.ea.tnt.rest.ListWithCursor;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;

import com.tnt.serenity.exception.DataNotFoundException;
import com.tnt.serenity.model.CustomEventSchema;
import com.ea.tnt.service.util.CacheException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

@Singleton
@Slf4j
public abstract class GuavaCustomEventSchemaAccess implements CustomEventSchemaAccess {
	
	private final LoadingCache<String, Optional<CustomEventSchema>> schemaCache;
	private final String INDEX_SEPARATOR = "_--_";
	
	public GuavaCustomEventSchemaAccess() {
		this.schemaCache = CacheBuilder.newBuilder()
				.maximumSize(250)
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.build(
						new CacheLoader<String, Optional<CustomEventSchema>>() {
							
							@Override
							public Optional<CustomEventSchema> load(String schemaId) throws Exception {
								CustomEventSchema schema;
								if (schemaId.contains("index" + INDEX_SEPARATOR)) {
									String[] split = schemaId.split(INDEX_SEPARATOR);
									String gameId = split[1];
									String eventName = split[2];
									schema = doGetSchemaByGameIdAndEventName(gameId, eventName);
									return Optional.of(schema);
								}
								schema = doGetSchemaById(schemaId);
								return Optional.of(schema);
							}
						});
	}

	@Override
	public abstract String nextCustomEventSchemaId();
	
	@Override
	public abstract boolean hasCustomEventSchemaId(String id);
	
	@Override
	public abstract boolean hasGameIdAndEventName(String gameId, String eventName);

	@Override
	public abstract void save(CustomEventSchema schema);
	
	@Override
	public CustomEventSchema getSchemaById(String id) {
		try {
			Optional<CustomEventSchema> customEventSchema = schemaCache.get(id);
			if (!customEventSchema.isPresent()) {
				throw new DataNotFoundException("No event schema found for id: " + id);
			}
			return customEventSchema.get();
		} catch (ExecutionException ex) {
			log.error("Exception retrieving custom event schema", ex);
			throw new CacheException("Exception retrieving custom event schema via cache", ex);
		} catch (UncheckedExecutionException ex) {
			//If we didn't find the schema cache a null to prevent further database look-ups on it
			if (ex.getCause() != null && ex.getCause().getClass() == DataNotFoundException.class) {
				addToCache(id, null, null, null);
				throw (DataNotFoundException) ex.getCause();
			} else {
				log.warn("Exception throw trying to retrieve custom event schema for cache", ex);
				throw ex;
			}
		}
	}
	
	protected abstract CustomEventSchema doGetSchemaById(String schemaId);
	
	@Override
	public CustomEventSchema getSchemaByGameIdAndEventName(final String gameId, final String eventName) {

		if(StringUtils.isBlank(gameId)) {
			throw new IllegalArgumentException("Expected valid gameId");
		}

		if(StringUtils.isBlank(eventName)) {
			throw new IllegalArgumentException("Expected valid eventName");
		}

		try {
			Optional<CustomEventSchema> customEventSchema = schemaCache.get("index" + INDEX_SEPARATOR + gameId + INDEX_SEPARATOR + eventName);
			if (!customEventSchema.isPresent()) {
				throw new DataNotFoundException("No event schema found for gameId " + gameId + " and eventName " + eventName);
			}
			return customEventSchema.get();
		} catch (ExecutionException ex) {
			log.error("Exception retrieving custom event schema", ex);
			throw new CacheException("Exception retrieving custom event schema via cache", ex);
		} catch (UncheckedExecutionException ex) {
			//If we didn't find the schema cache a null to prevent further database look-ups on it
			if (ex.getCause() != null && ex.getCause().getClass() == DataNotFoundException.class) {
				addToCache(null, gameId, eventName, null);
				throw (DataNotFoundException) ex.getCause();
			} else {
				log.warn("Exception throw trying to retrieve custom event schema for cache", ex);
				throw ex;
			}
		}
	}
	
	protected abstract CustomEventSchema doGetSchemaByGameIdAndEventName(String gameId, String eventName);
	
	@Override
	public abstract ListWithCursor<CustomEventSchema> listSchemaByGameId(String gameId);
	
	@Override
	public CustomEventSchema update(CustomEventSchema schema) {
		CustomEventSchema savedSchema = doUpdate(schema);
		addToCache(savedSchema);
		log.debug("Custom event schema {} added to cache", savedSchema.getId());
		return savedSchema;
	}
	
	protected abstract CustomEventSchema doUpdate(CustomEventSchema schema);
	
	@Override
	public void delete(CustomEventSchema schema) {
		schemaCache.invalidate(schema.getId());
		schemaCache.invalidate("index" + INDEX_SEPARATOR + schema.getGameId() + INDEX_SEPARATOR + schema.getEventName());
		doDelete(schema);
	}
	
	protected abstract void doDelete(CustomEventSchema schema);
	
	private void addToCache(CustomEventSchema schema) {
		addToCache(schema.getId(), schema.getGameId(), schema.getEventName(), schema);
	}
	
	private void addToCache(String id, String gameId, String eventName, CustomEventSchema schema) {
		
		Optional<CustomEventSchema> optionalSchema;
		if (schema == null) {
			optionalSchema = Optional.absent();
		} else {
			optionalSchema = Optional.of(schema);
		}
		
		if (id != null) {
			schemaCache.put(id, optionalSchema);
		}
		if (gameId != null && eventName != null) {
			schemaCache.put("index" + INDEX_SEPARATOR + gameId + INDEX_SEPARATOR + eventName, optionalSchema);
		}
	}
	
}
