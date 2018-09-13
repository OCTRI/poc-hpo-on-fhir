package org.octri.hpoonfhir.service;

import java.util.HashMap;
import java.util.Map;

import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.loinc.exception.LoincException;
import org.octri.hpoonfhir.view.ReferenceRange;
import org.springframework.stereotype.Service;

@Service
public class ReferenceRangeService {
	
	Map<LoincId, ReferenceRange> referenceRanges = new HashMap<>();
	
	public ReferenceRangeService() throws LoincException {
		LoincId loincId = new LoincId("2339-0");
		ReferenceRange range = new ReferenceRange(70.0, 130.0);
		referenceRanges.put(loincId, range);
	}
	
	public ReferenceRange getReferenceRangeForLoinc(LoincId loincId) {
		return referenceRanges.get(loincId);
	}

}
