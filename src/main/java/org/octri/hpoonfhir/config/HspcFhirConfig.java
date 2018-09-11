package org.octri.hpoonfhir.config;

import javax.inject.Inject;

import org.hspconsortium.client.auth.StateProvider;
import org.hspconsortium.client.auth.access.AccessTokenProvider;
import org.hspconsortium.client.auth.access.JsonAccessTokenProvider;
import org.hspconsortium.client.auth.authorizationcode.AuthorizationCodeRequestBuilder;
import org.hspconsortium.client.auth.credentials.ClientSecretCredentials;
import org.hspconsortium.client.controller.FhirEndpointsProvider;
import org.hspconsortium.client.controller.FhirEndpointsProviderDSTU2;
import org.hspconsortium.client.controller.FhirEndpointsProviderSTU3;
import org.hspconsortium.client.session.ApacheHttpClientFactory;
import org.hspconsortium.client.session.FhirSessionContextHolder;
import org.hspconsortium.client.session.SessionKeyRegistry;
import org.hspconsortium.client.session.authorizationcode.AuthorizationCodeSessionFactory;
import org.hspconsortium.client.session.impl.SimpleFhirSessionContextHolder;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.service.Stu2FhirService;
import org.octri.hpoonfhir.service.Stu3FhirService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.IRestfulClientFactory;

/**
 * The configuration needed to work with the HSPC Java Client.
 * @author yateam
 *
 */
@Configuration
public class HspcFhirConfig {
	
	@Value("${fhir-server-configuration.name}")
    private String fhirServiceName;
	
	@Value("${fhir-server-configuration.version}")
    private String fhirVersion;
	
    @Value("${fhir-server-configuration.clientId}")
    private String clientId;

    @Value("${fhir-server-configuration.scope}")
    private String scope;

    @Value("${fhir-server-configuration.redirectUri}")
    private String redirectUri;

    @Value("${fhir-server-configuration.clientSecret}")
    private String clientSecret;

    @Value("${fhir-server-configuration.appEntryPoint}")
    private String appEntryPoint;

    @Value("${fhir-server-configuration.httpConnectionTimeoutMilliSeconds:}")
    private String httpConnectionTimeoutMilliSeconds;

    @Value("${fhir-server-configuration.httpReadTimeoutMilliSeconds:}")
    private String httpReadTimeoutMilliSeconds;

    @Bean
    public String fhirServiceName() {
        return fhirServiceName;
    }

    @Bean
    public String fhirVersion() {
        return fhirVersion;
    }

    @Bean
    public String clientId() {
        return clientId;
    }

    @Bean
    public String scope() {
        return scope;
    }

    @Bean
    public String redirectUri() {
        return redirectUri;
    }

    @Bean
    public String clientSecret() {
        return clientSecret;
    }

    @Bean
    public String appEntryPoint() {
        return appEntryPoint;
    }

    @Bean
    public Integer httpConnectionTimeOut() {
        return Integer.parseInt(
                httpConnectionTimeoutMilliSeconds != null && httpConnectionTimeoutMilliSeconds.length() > 0
                        ? httpConnectionTimeoutMilliSeconds
                        : IRestfulClientFactory.DEFAULT_CONNECT_TIMEOUT + "");
    }

    @Bean
    public Integer httpReadTimeOut() {
        return Integer.parseInt(
                httpReadTimeoutMilliSeconds != null && httpReadTimeoutMilliSeconds.length() > 0
                        ? httpReadTimeoutMilliSeconds
                        : IRestfulClientFactory.DEFAULT_CONNECTION_REQUEST_TIMEOUT + "");
    }

    @Bean
	public String proxyPassword() {
		return System.getProperty("http.proxyPassword", System.getProperty("https.proxyPassword"));
	}

	@Bean
	public String proxyUser() {
		return System.getProperty("http.proxyUser", System.getProperty("https.proxyUser"));
	}

	@Bean
	public Integer proxyPort() {
		return Integer.parseInt(System.getProperty("http.proxyPort", System.getProperty("https.proxyPort", "8080")));
	}

	@Bean
	public String proxyHost() {
		// To Use With Proxy
		// -Dhttp.proxyHost=proxy.host.com -Dhttp.proxyPort=8080 -Dhttp.proxyUser=username -Dhttp.proxyPassword=password
		return System.getProperty("http.proxyHost", System.getProperty("https.proxyHost"));
	}

	@Bean
	public FhirContext fhirContext(String fhirVersion, Integer httpConnectionTimeOut, Integer httpReadTimeOut) {
		
		FhirVersionEnum v = FhirVersionEnum.valueOf(FhirVersionEnum.class, fhirVersion);
		FhirContext hapiFhirContext = new FhirContext(v);
		// Set how long to try and establish the initial TCP connection (in ms)
		hapiFhirContext.getRestfulClientFactory().setConnectTimeout(httpConnectionTimeOut);

		// Set how long to block for individual read/write operations (in ms)
		hapiFhirContext.getRestfulClientFactory().setSocketTimeout(httpReadTimeOut);

		return hapiFhirContext;
	}
	
	@Bean
	public FhirService fhirService(FhirContext fhirContext, String fhirServiceName) {
		if (fhirContext.getVersion().getVersion().equals(FhirVersionEnum.DSTU2)) {
			return new Stu2FhirService(fhirServiceName);
		} else if (fhirContext.getVersion().getVersion().equals(FhirVersionEnum.DSTU3)) {
			return new Stu3FhirService(fhirServiceName);
		}
		throw new IllegalArgumentException("The FHIR version " + fhirContext.getVersion().getVersion() + " is not supported.");
	}

	@Bean
	public FhirEndpointsProvider fhirEndpointsProvider(FhirContext fhirContext) {
		if (fhirContext.getVersion().getVersion().equals(FhirVersionEnum.DSTU2)) {
			return new FhirEndpointsProviderDSTU2(fhirContext);
		} else if (fhirContext.getVersion().getVersion().equals(FhirVersionEnum.DSTU3)) {
			return new FhirEndpointsProviderSTU3(fhirContext);
		}
		throw new IllegalArgumentException("The FHIR version " + fhirContext.getVersion().getVersion() + " is not supported.");
	}

	@Bean
	public StateProvider stateProvider() {
		return new StateProvider.DefaultStateProvider();
	}

	@Bean
	public FhirSessionContextHolder fhirSessionContextHolder() {
		return new SimpleFhirSessionContextHolder();
	}

	@Bean
	@Inject
	public ApacheHttpClientFactory apacheHttpClientFactory(Integer httpConnectionTimeOut,
		Integer httpReadTimeOut) {
		// TODO: Figure out how to work with proxy
		return new ApacheHttpClientFactory(null, null, null, null,
			httpConnectionTimeOut, httpReadTimeOut);
	}

	@Bean
	public AccessTokenProvider accessTokenProvider(ApacheHttpClientFactory apacheHttpClientFactory) {
		return new JsonAccessTokenProvider(apacheHttpClientFactory);
	}

    @Bean
    @Inject
    public AuthorizationCodeRequestBuilder authorizationCodeRequestBuilder(FhirEndpointsProvider fhirEndpointsProvider,
                                                                           StateProvider stateProvider) {
        return new AuthorizationCodeRequestBuilder(fhirEndpointsProvider, stateProvider);
    }

    @Bean
    @Inject
    public ClientSecretCredentials clientSecretCredentials(String clientSecret) {
        return new ClientSecretCredentials(clientSecret);
    }

    @Bean
    public SessionKeyRegistry sessionKeyRegistry() {
        return new SessionKeyRegistry();
    }

    @Bean
    @Inject
    public AuthorizationCodeSessionFactory<ClientSecretCredentials>
    authorizationCodeSessionFactory(FhirContext fhirContext, SessionKeyRegistry sessionKeyRegistry,
                                    FhirSessionContextHolder fhirSessionContextHolder,
                                    AccessTokenProvider patientAccessTokenProvider,
                                    String clientId, ClientSecretCredentials clientSecretCredentials, String redirectUri) {
        return new AuthorizationCodeSessionFactory<>(fhirContext, sessionKeyRegistry, "MySessionKey", fhirSessionContextHolder,
                patientAccessTokenProvider, clientId, clientSecretCredentials, redirectUri);
    }
}
