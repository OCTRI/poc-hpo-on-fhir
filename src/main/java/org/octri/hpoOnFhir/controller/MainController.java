package org.octri.hpoOnFhir.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.monarchinitiative.loinc2hpo.loinc.LOINC2HpoAnnotationImpl;
import org.monarchinitiative.loinc2hpo.loinc.LoincId;
import org.octri.hpoOnFhir.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {
	
	@Autowired
	AnnotationService annotationService;

	@RequestMapping("/")
	public String search(Map<String, Object> model, HttpServletRequest request) {
		return "search";
	}

	@RequestMapping("/labs")
	public String labs(Map<String, Object> model, HttpServletRequest request) {
		model.put("patient_id", request.getParameter("patient_id"));
		return "labs";
	}

	@RequestMapping("/annotations")
	public String annotations(Map<String, Object> model) throws Exception {
		model.put("loincMap", annotationService.getAnnotations().entrySet());
		
		return "annotations";
	}
	
	@RequestMapping("/hpo")
	public String hpo(Map<String, Object> model, HttpServletRequest request) throws Exception {
		String loincId = request.getParameter("loinc_id");
		LOINC2HpoAnnotationImpl annotation = annotationService.getAnnotations().get(new LoincId(loincId));
		model.put("annotation", annotation.toString());
		
		return "hpo";
	}

}
