package org.octri.hpoonfhir.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.monarchinitiative.fhir2hpo.loinc.DefaultLoinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.Loinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.service.AnnotationService;
import org.octri.hpoonfhir.service.Stu2FhirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

	@Autowired
	AnnotationService annotationService;
	
	@Autowired
	Stu2FhirService epicFhirService;

	@RequestMapping("/")
	public String search(Map<String, Object> model, HttpServletRequest request) {
		try {
			epicFhirService.findPatientsByFullName("Jason", "Argonaut");
		} catch (Exception e) {
			
		}
		return "search";
	}

	@RequestMapping("/labs")
	public String labs(Map<String, Object> model, HttpServletRequest request) {
		model.put("patient_id", request.getParameter("patient_id"));
		return "labs";
	}

	@RequestMapping("/annotations")
	public String annotations(Map<String, Object> model) throws Exception {
		Map<LoincId, Loinc2HpoAnnotation> annotations = annotationService.getAnnotationsMap();
		List<LoincId> loincs = annotations.entrySet().stream()
				.filter(x -> x.getValue() instanceof DefaultLoinc2HpoAnnotation).map(x -> x.getKey())
				.collect(Collectors.toList());
		model.put("loincs", loincs);

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
