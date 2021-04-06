package com.tnt.serenity.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DataNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 965651293587333249L;

	public DataNotFoundException(final String msg) {
		super(msg);
	}
}
