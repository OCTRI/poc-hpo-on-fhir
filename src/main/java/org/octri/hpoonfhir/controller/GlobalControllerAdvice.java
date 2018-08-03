package org.octri.hpoonfhir.controller;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {
	
	@Value("${app.name}")
	private String appName;

	@Value("${app.version}")
	private String appVersion;

	@Value("${app.displayName}")
	private String displayName;

	@ModelAttribute
	public void addDefaultAttributes(HttpServletRequest req, Model model) {
		model.addAttribute("appName", appName);
		model.addAttribute("appVersion", appVersion);
		model.addAttribute("displayName", displayName);
		model.addAttribute("currentYear", Calendar.getInstance().get(Calendar.YEAR));

	}


}
