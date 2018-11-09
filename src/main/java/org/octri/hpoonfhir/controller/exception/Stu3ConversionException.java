package org.octri.hpoonfhir.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Gracefully handle the exception if the resource cannot be converted to DSTU3.
 * @author yateam
 *
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class Stu3ConversionException extends RuntimeException {
	
	public Stu3ConversionException() {
		super("The resource(s) could not be converted to DSTU3");
	}
	
}
