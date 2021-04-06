package com.tnt.serenity.exception;

public class EnqueueFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	public EnqueueFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
