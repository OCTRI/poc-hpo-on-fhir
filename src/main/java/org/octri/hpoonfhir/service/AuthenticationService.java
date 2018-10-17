package org.octri.hpoonfhir.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

	@Autowired
	OAuth2AuthorizedClientService oauthService;

	public String getTokenString(OAuth2AuthenticationToken authentication) {

		// TODO: Example of extracting the token stored in the client service for API requests. The FHIRService
		// has to accept a token for any request and construct a new client with the auth
		// interceptor each time. Seems like there should be a better way? The RDWServiceClient doesn't do this.
		// Some articles:
		// https://spring.io/blog/2018/03/06/using-spring-security-5-to-integrate-with-oauth-2-secured-services-such-as-facebook-and-github
		// https://spring.io/guides/tutorials/spring-boot-oauth2/
		OAuth2AuthorizedClient client = oauthService.loadAuthorizedClient(
			authentication.getAuthorizedClientRegistrationId(),
			authentication.getName());
		
		return client.getAccessToken().getTokenValue();

	}
}
