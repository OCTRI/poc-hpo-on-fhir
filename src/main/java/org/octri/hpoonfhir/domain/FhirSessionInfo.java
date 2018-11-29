package org.octri.hpoonfhir.domain;

import org.octri.hpoonfhir.controller.exception.UnauthorizedException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * Store information for the session
 * 
 * @author yateam
 *
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class FhirSessionInfo {

	private String state;
	private String token;

	public Boolean hasState() {
		return state != null;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Assert that the session has a token. Return the token or throw an AnauthorizedException if one doesn't exist.
	 * @return
	 */
	public String assertToken() {
		if (token == null) {
			throw new UnauthorizedException();
		}
		return token;
	}


}
