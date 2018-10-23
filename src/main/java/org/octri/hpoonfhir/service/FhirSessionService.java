package org.octri.hpoonfhir.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;


/**
 * Stores an in memory map of sessions. This is not a production level solution.
 * @author yateam
 *
 */
@Service
public class FhirSessionService {
	
	// Simple map from the JSESSIONID to the authentication token
	Map<String,String> sessionMap = new HashMap<String,String>();
	
	public void putSession(HttpServletRequest request, String authToken) {
		Cookie[] cookies = request.getCookies();
		Optional<Cookie> cookie = Arrays.stream(cookies).filter(c -> c.getName().equals("JSESSIONID")).findFirst();
		if (cookie.isPresent()) {
			sessionMap.put(cookie.get().getValue(), authToken);
		}
	}
	
	public String getSessionToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		Optional<Cookie> cookie = Arrays.stream(cookies).filter(c -> c.getName().equals("JSESSIONID")).findFirst();
		if (cookie.isPresent()) {
			return sessionMap.get(cookie.get().getValue());
		}
		return null;
	}
	
}
