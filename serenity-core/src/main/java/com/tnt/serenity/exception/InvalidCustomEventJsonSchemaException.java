package com.tnt.serenity.exception;

import com.tnt.serenity.validation.ValidationResult;

public class InvalidCustomEventJsonSchemaException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3050230610395805392L;
	
	private ValidationResult report;

	public InvalidCustomEventJsonSchemaException(final ValidationResult report) {
		super(report.getFieldMessages().toString());
		this.report = report;
	}
	
	public ValidationResult getReport() {
		return report;
	}
}
