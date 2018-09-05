package org.octri.hpoonfhir.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static List<String> clients = Arrays.asList("google", "epic", "smarthealth");

	private static String CLIENT_PROPERTY_KEY = "spring.security.oauth2.client.registration.";

	@Autowired
	private Environment env;

	private ClientRegistration getRegistration(String client) {
		String clientId = env.getProperty(
			CLIENT_PROPERTY_KEY + client + ".client-id");

		if (clientId == null) {
			return null;
		}

		String clientSecret = env.getProperty(
			CLIENT_PROPERTY_KEY + client + ".client-secret");

		if (client.equals("google")) {
			return CommonOAuth2Provider.GOOGLE.getBuilder(client)
				.clientId(clientId).clientSecret(clientSecret).build();
		}
		if (client.equals("epic")) {
			ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(client);
			builder.clientAuthenticationMethod(ClientAuthenticationMethod.BASIC);
			builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
			builder.scope("launch");
			builder.redirectUriTemplate("http://www.locolhost.com:8080/login/oauth2/code/{registrationId}");			
			builder.authorizationUri("https://apporchard.epic.com/interconnect-aocurprd-oauth/oauth2/authorize");
			builder.tokenUri("https://apporchard.epic.com/interconnect-aocurprd-oauth/oauth2/token");
			builder.clientName("Epic");
			return builder.clientId(clientId).clientSecret(clientSecret).build();
		}
		if (client.equals("smarthealth")) {
			String fhirUrl = "https://launch.smarthealthit.org/v/r3/sim/eyJoIjoiMSIsImkiOiIxIiwiaiI6IjEiLCJlIjoic21hcnQtUHJhY3RpdGlvbmVyLTc4ODAzNzgsc21hcnQtUHJhY3RpdGlvbmVyLTcxNDgyNzEzIn0/";
			ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(client);
			builder.clientAuthenticationMethod(ClientAuthenticationMethod.BASIC);
			builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
			builder.scope("openid", "profile", "launch");
			builder.redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}");			
			builder.authorizationUri(fhirUrl + "auth/authorize?aud=" + fhirUrl + "fhir");
			builder.tokenUri(fhirUrl + "auth/token");
			builder.clientName("Smart Health IT");
			builder.userInfoUri(fhirUrl + "fhir/Practioner/smart-Practitioner-71482713");
			builder.jwkSetUri("https://launch.smarthealthit.org/keys");
			return builder.clientId(clientId).clientSecret(clientSecret).build();
		}
		return null;
	}

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		List<ClientRegistration> registrations = clients.stream()
			.map(c -> getRegistration(c))
			.filter(registration -> registration != null)
			.collect(Collectors.toList());

		return new InMemoryClientRegistrationRepository(registrations);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/css/*", "/images/*", "/js/*", "/webjars/**", "/oauth_login", "/actuator/**")
			.permitAll()
			.anyRequest()
			.authenticated()
			.and()
			.oauth2Login()
			.loginPage("/oauth_login")
			.clientRegistrationRepository(clientRegistrationRepository())
			.authorizedClientService(authorizedClientService());
	}

	@Bean
	public OAuth2AuthorizedClientService authorizedClientService() {

		return new InMemoryOAuth2AuthorizedClientService(
			clientRegistrationRepository());
	}
	
	@Bean
	public List<String> registrationIds() {
		return clients;
	}

}