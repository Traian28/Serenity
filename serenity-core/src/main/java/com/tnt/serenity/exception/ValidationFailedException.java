package com.tnt.serenity.exception;

public class ValidationFailedException extends RuntimeException {

	public ValidationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
