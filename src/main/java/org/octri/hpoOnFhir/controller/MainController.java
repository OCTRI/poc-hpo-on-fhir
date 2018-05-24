package org.octri.hpoOnFhir.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.monarchinitiative.fhir2hpo.loinc.Loinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.service.AnnotationService;
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
		model.put("loincMap", annotationService.getAnnotationsMap().entrySet());
		
		return "annotations";
	}
	
	@RequestMapping("/hpo")
	public String hpo(Map<String, Object> model, HttpServletRequest request) throws Exception {
		String loincId = request.getParameter("loinc_id");
		Loinc2HpoAnnotation annotation = annotationService.getAnnotations(new LoincId(loincId));
		model.put("annotation", annotation.toString());
		
		return "hpo";
	}

}
