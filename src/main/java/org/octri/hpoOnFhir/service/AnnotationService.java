package org.octri.hpoOnFhir.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.monarchinitiative.loinc2hpo.io.HPOParser;
import org.monarchinitiative.loinc2hpo.io.LoincAnnotationSerializationFactory;
import org.monarchinitiative.loinc2hpo.io.LoincAnnotationSerializationFactory.SerializationFormat;
import org.monarchinitiative.loinc2hpo.loinc.LOINC2HpoAnnotationImpl;
import org.monarchinitiative.loinc2hpo.loinc.LoincId;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

/**
 * This Service keeps the in-memory annotation map that will be used to convert FHIR observations into HPO terms
 * 
 * @author yateam
 *
 */
@Service
public class AnnotationService {
	
	Map<LoincId, LOINC2HpoAnnotationImpl> loincMap;
	
	public AnnotationService() throws Exception {
		HPOParser hpoOboParser = new HPOParser("/Users/yateam/Documents/LOINC2HPO/hp.obo");
        HpoOntology ontology = hpoOboParser.getHPO();
        ImmutableMap.Builder<TermId,Term> termmapBuilder = new ImmutableMap.Builder<>();
        // for some reason there is a bug here...issue #34 on ontolib tracker
        // here is a workaround to remove duplicate entries
        List<Term> res = ontology.getTermMap().values().stream().distinct()
                .collect(Collectors.toList());

        res.forEach( term -> termmapBuilder.put(term.getId(), term));
        ImmutableMap<TermId, Term> termmap = termmapBuilder.build();

        this.loincMap = LoincAnnotationSerializationFactory.parseFromFile("/Users/yateam/Git/loinc2hpoAnnotation/Data/TSVSingleFile/annotations.tsv", termmap, SerializationFormat.TSVSingleFile);
	}
	
	public Map<LoincId, LOINC2HpoAnnotationImpl> getAnnotations() {
		return loincMap;
	}
	

}
