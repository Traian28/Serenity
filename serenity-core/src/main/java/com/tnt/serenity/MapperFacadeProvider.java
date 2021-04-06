package com.tnt.serenity;

import com.google.inject.Inject;
import com.google.inject.Provider;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;

public class MapperFacadeProvider implements Provider<MapperFacade> {

	private final MapperFactory mapperFactory;

	@Inject
	public MapperFacadeProvider() {
		mapperFactory = new DefaultMapperFactory.Builder().build();
		mapperFactory.getConverterFactory().registerConverter(
				new PassThroughConverter(org.joda.time.DateTime.class));
	}

	@Override
	public MapperFacade get() {
		return mapperFactory.getMapperFacade();
	}

}
