package org.octri.hpoonfhir.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.octri.hpoonfhir.service.JhuGroupService;
import org.octri.hpoonfhir.service.ReferenceRangeService;
import org.octri.hpoonfhir.view.DecileModel;
import org.octri.hpoonfhir.view.LabSummaryModel;
import org.octri.hpoonfhir.view.ReferenceRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controller for viewing the JHU clinical profile asthma data
 * @author yateam
 *
 */
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
	
	@Autowired
	JhuGroupService jhuGroupService;
	
	@GetMapping("profile")
	public String getProfiles(Map<String, Object> model) throws JsonParseException, JsonMappingException, IOException {
		List<Map<String,Object>> labs = new ArrayList<>();
		for (int i=1; i<=96; i++) {
			Long id = new Long(i);
			Map<String,Object> lab = new HashMap<>();
			lab.put("id", id);
			Map<String, Object> map = getResourceAsMap(id);
			lab.put("description", getProfileDescription((Map<String,Object>) map.get("text")));			
			Map<String,String> group = jhuGroupService.getAsthmaGroupForId(id);
			lab.putAll(group);
			labs.add(lab);
		}
		model.put("labs", labs);
		return "profile/list";
	}


	/**
	 * Get the description of the profile
	 * @param lab
	 * @param text
	 * @return
	 */
	private String getProfileDescription(Map<String, Object> text) {
		// Hacky way to pull out the text describing the cohort
		String div = (String) text.get("div");
		Pattern p = Pattern.compile("<p>(.+)");
		Matcher m = p.matcher(div);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}


	@GetMapping("/profile/{id}")
	public String getProfiles(Map<String, Object> model, @PathVariable Long id) throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> map = getResourceAsMap(id);
		model.put("description", getProfileDescription((Map<String,Object>) map.get("text")));
		List<Map<String, Object>> labs = (List<Map<String, Object>>) map.get("lab");
		List<LabSummaryModel> labSummaries = summarizeLabs(labs);
		Collections.sort(labSummaries, (o, n) -> o.getCount().compareTo(n.getCount()));
		Collections.reverse(labSummaries);
		model.put("id", id);
		model.put("labs", labSummaries);
		model.put("includeProfilesJs", true);
		return "profile/view";
	}

	/**
	 * Given the resource number, get the JSON file as a map.
	 * @param id
	 * @return A map representing the json file extracted from the resource
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	private Map<String, Object> getResourceAsMap(Long id)
		throws IOException, JsonParseException, JsonMappingException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream stream = classLoader.getResourceAsStream("json/jhu-asthma-lab-" + id + ".json");
		ObjectMapper om = new ObjectMapper();
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};
		HashMap<String, Object> map = om.readValue(stream, typeRef);
		return map;
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
			model.setCount(getInteger(lab.get("count")));
			model.setFrequencyPerYear(getDouble(lab.get("frequencyPerYear")));
			model.setFractionOfSubjects(getDouble(lab.get("fractionOfSubjects")));
			Map<String, Object> scalarDistribution = (Map<String, Object>) lab.get("scalarDistribution");
			Map<String,Object> units = (Map<String, Object>) scalarDistribution.get("units");
			model.setUnit((String) units.get("unit")); 
			model.setMin(getDouble(scalarDistribution.get("min")));
			model.setMax(getDouble(scalarDistribution.get("max")));
			model.setMean(getDouble(scalarDistribution.get("mean")));
			model.setMedian(getDouble(scalarDistribution.get("median")));
			model.setStddev(getDouble(scalarDistribution.get("stdDev")));
			model.setFractionAboveNormal(getDouble(scalarDistribution.get("fractionAboveNormal")));
			model.setFractionBelowNormal(getDouble(scalarDistribution.get("fractionBelowNormal")));
			model.setDeciles(summarizeDeciles(model.getReferenceRange(), model.getRelatedHpoTerms(), (List<Map<String,Object>>) scalarDistribution.get("decile")));
			models.add(model);
		}
		return models;
	}

	private List<DecileModel> summarizeDeciles(ReferenceRange referenceRange, List<Map<String, String>> relatedHpoTerms,
		List<Map<String, Object>> deciles) {
		
		List<DecileModel> decileModels = new ArrayList<>();
		for (Map<String,Object> decile : deciles) {
			DecileModel decileModel = new DecileModel();
			decileModel.setNth(getDouble(decile.get("nth"))); 
			Double val = getDouble(decile.get("value"));
			decileModel.setValue(val);
			if (referenceRange != null && relatedHpoTerms != null) {
				String code = "N";
				if (val < referenceRange.getMin()) {
					code = "L";
				} else if (val > referenceRange.getMax()) {
					code = "H";
				} 
				for (Map<String,String> termMap : relatedHpoTerms) {
					if (termMap.get("code").equals(code)) {
						decileModel.setHpo(termMap.get("term"));
					}
				}
			}
			decileModels.add(decileModel);
		}

		return decileModels;
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

	private Integer getInteger(Object number) {
		if (number == null) {
			return null;
		}
		if (number instanceof Integer) {
			return (Integer) number;
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
