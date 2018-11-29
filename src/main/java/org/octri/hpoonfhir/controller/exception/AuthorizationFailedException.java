package org.octri.hpoonfhir.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This denotes a general exception during the Authorization process and could occur for
 * a variety of reasons.
 * 
 * @author yateam
 *
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AuthorizationFailedException extends RuntimeException {
	
	public AuthorizationFailedException() {
		super("Authorization to the FHIR server failed.");
	}
	
}
