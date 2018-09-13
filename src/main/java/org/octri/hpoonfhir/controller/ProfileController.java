package org.octri.hpoonfhir.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.monarchinitiative.fhir2hpo.loinc.DefaultLoinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.Loinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.loinc.exception.LoincException;
import org.monarchinitiative.fhir2hpo.loinc.exception.LoincNotAnnotatedException;
import org.monarchinitiative.fhir2hpo.service.AnnotationService;
import org.monarchinitiative.fhir2hpo.service.HpoService;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermPrefix;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.service.ReferenceRangeService;
import org.octri.hpoonfhir.view.LabSummaryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class ProfileController {

	@Autowired
	FhirService fhirService;

	@Autowired
	AnnotationService annotationService;

	@Autowired
	HpoService hpoService;
	
	@Autowired
	ReferenceRangeService referenceRangeService;

	@GetMapping("/profiles")
	public String getProfiles(Map<String, Object> model) throws JsonParseException, JsonMappingException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream stream = classLoader.getResourceAsStream("json/jhu-asthma-lab-1.json");
		ObjectMapper om = new ObjectMapper();
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};
		HashMap<String, Object> map = om.readValue(stream, typeRef);
		List<Map<String, Object>> labs = (List<Map<String, Object>>) map.get("lab");
		List<LabSummaryModel> labSummaries = summarizeLabs(labs);
		model.put("labs", labSummaries);
		model.put("includeProfilesJs", true);
		return "profiles";
	}

	private List<LabSummaryModel> summarizeLabs(List<Map<String, Object>> labs) {
		List<LabSummaryModel> models = new ArrayList<>();
		for (Map<String, Object> lab : labs) {
			LabSummaryModel model = new LabSummaryModel();
			Map<String, Object> coding = getCodingForLab(lab);
			try {
				LoincId loincId = new LoincId((String) coding.get("code"));
				model.setLoincId(loincId);
				model.setReferenceRange(referenceRangeService.getReferenceRangeForLoinc(loincId));
				Loinc2HpoAnnotation annotation = annotationService.getAnnotations(loincId);
				model.setAnnotated(true);
				if (annotation instanceof DefaultLoinc2HpoAnnotation) {
					model.setRelatedHpoTerms(getHpoTerms((DefaultLoinc2HpoAnnotation) annotation));
				} else {
					model.setAnnotated(false);
				}
			} catch (LoincNotAnnotatedException e) {
				model.setAnnotated(false);
			} catch (LoincException e) {
				// bad json!
				e.printStackTrace();
			}
			model.setDescription((String) coding.get("display"));
			Map<String, Object> scalarDistribution = (Map<String, Object>) lab.get("scalarDistribution");
			model.setMin(getDouble(scalarDistribution.get("min")));
			model.setMax(getDouble(scalarDistribution.get("max")));
			model.setMean(getDouble(scalarDistribution.get("mean")));
			model.setMedian(getDouble(scalarDistribution.get("median")));
			model.setFractionAboveNormal(getDouble(scalarDistribution.get("fractionAboveNormal")));
			model.setFractionBelowNormal(getDouble(scalarDistribution.get("fractionBelowNormal")));
			models.add(model);
		}
		return models;
	}

	private Map<String, Object> getCodingForLab(Map<String, Object> lab) {
		List<Map<String, Object>> codes = (List<Map<String, Object>>) lab.get("code");
		List<Map<String, Object>> coding = (List<Map<String, Object>>) codes.get(0).get("coding");
		return coding.get(0);
	}

	private Double getDouble(Object number) {
		if (number == null) {
			return null;
		}
		if (number instanceof Double) {
			return (Double) number;
		} else if (number instanceof Integer) {
			Integer n = (Integer) number;
			return n.doubleValue();
		}
		System.out.println("Problem converting the number " + number);
		return null;
	}

	private List<Map<String, String>> getHpoTerms(DefaultLoinc2HpoAnnotation annotation) {
		List<Map<String, String>> relatedHpoTerms = new ArrayList<>();
		String json = annotation.toString();
		ObjectMapper om = new ObjectMapper();
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};
		try {
			HashMap<String, Object> mappingsObj = om.readValue(json, typeRef);
			List<Map<String, Object>> mappings = (List<Map<String, Object>>) mappingsObj.get("mappings");
			for (Map<String, Object> mapping : mappings) {
				Map<String, String> map = new HashMap<>();
				Boolean negated = Boolean.parseBoolean((String) mapping.get("termNegated"));
				TermPrefix prefix = new TermPrefix((String) mapping.get("termPrefix"));
				TermId termId = new TermId(prefix, (String) mapping.get("termId"));
				Term term = hpoService.getTermForTermId(termId);
				map.put("code", (String) mapping.get("code"));
				map.put("term", (negated ? "NOT " : "") + term.getName());
				relatedHpoTerms.add(map);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return relatedHpoTerms;
	}

}
