package org.octri.hpoonfhir.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Present the login page with the list of OAuth servers available
 * @author yateam
 *
 */
@Controller
public class LoginController {

	@Autowired
	List<String> registrationIds;
	
	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;
	
	@GetMapping("/oauth_login")
	public String getLoginPage(Model model) {
		
		List<Map<String, String>> oauth2AuthenticationUrls = new ArrayList<>();
		registrationIds.forEach(registrationId -> {
			ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(registrationId);
			Map<String,String> clientMap = new HashMap<>();
			clientMap.put("name", registration.getClientName());
			clientMap.put("auth", "oauth2/authorization/" + registration.getRegistrationId());
			oauth2AuthenticationUrls.add(clientMap);
		});
		model.addAttribute("urls", oauth2AuthenticationUrls);

		return "oauth_login";
	}
}
